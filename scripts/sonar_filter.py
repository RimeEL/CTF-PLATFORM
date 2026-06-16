import requests
import joblib
import numpy as np
import json
import os
import sys
import types
from gensim.models import Word2Vec

# The w2v model was pickled with a 'tools' package (tools.callbacks.EpochSaver
# and possibly others). Stub out the entire tools.* namespace: each stub module
# dynamically creates empty classes for any attribute pickle looks up, with a
# no-op __setstate__ so stored callback state is silently absorbed.
class _StubBase:
    def __init__(self, *_args, **_kwargs): pass
    def __setstate__(self, _state): pass

class _StubModule(types.ModuleType):
    def __getattr__(self, name):
        stub_cls = type(name, (_StubBase,), {})
        setattr(self, name, stub_cls)
        return stub_cls

class _StubFinder:
    def find_module(self, name, path=None):
        if name == "tools" or name.startswith("tools."):
            return self

    def load_module(self, name):
        if name not in sys.modules:
            mod = _StubModule(name)
            mod.__path__ = []
            mod.__package__ = name
            sys.modules[name] = mod
        return sys.modules[name]

sys.meta_path.insert(0, _StubFinder())

print("[INFO] Chargement des modèles...")
model      = joblib.load("scripts/lgbm_model.pkl")
w2v        = Word2Vec.load("scripts/w2v_model.model")
squid_list = json.load(open("scripts/squid_classes.json"))

SONAR_TOKEN   = os.environ["SONAR_TOKEN"]
PROJECT_KEY   = os.environ.get("SONAR_PROJECT_KEY", "RimeEL_CTF-PLATFORM")
SONAR_URL     = "https://sonarcloud.io"


# Règles dont la criticité est certaine — classées TP sans passer par le modèle ML.
FORCED_TRUE_POSITIVES = {
    "secrets:S2068",  # credentials hardcodés (secrets)
    "secrets:S6290",  # credentials hardcodés (secrets)
    "java:S6437",     # passwords compromis (BLOCKER)
    "java:S2226",     # champ mutable dans Servlet partagé → race condition
    "java:S5122",     # CORS permissif → risque CSRF
    "java:S2068",     # passwords hardcodés (variante java)
}


def fetch_all_issues():
    issues = []
    page = 1
    total = None
    while True:
        resp = requests.get(
            f"{SONAR_URL}/api/issues/search",
            params={"componentKeys": PROJECT_KEY, "ps": 500, "p": page},
            auth=(SONAR_TOKEN, ""),
        )
        resp.raise_for_status()
        data = resp.json()
        if total is None:
            total = data["total"]
        issues.extend(data["issues"])
        if page * 500 >= total:
            break
        page += 1
    return issues


def normalize_rule(rule):
    rule = rule.replace("java:", "squid_")
    rule = rule.replace("squid:", "squid_")
    rule = rule.replace(":", "_")
    return rule


def embed_text(text):
    if not text:
        return np.zeros(64)
    tokens = text.lower().split()
    vecs = [w2v.wv[t] for t in tokens if t in w2v.wv]
    if not vecs:
        return np.zeros(64)
    return np.mean(vecs, axis=0)


def build_feature(issue):
    normalized = normalize_rule(issue.get("rule", ""))

    one_hot = np.zeros(160)
    if normalized in squid_list:
        one_hot[squid_list.index(normalized)] = 1

    line0_emb = embed_text(issue.get("message", ""))

    return np.concatenate([
        one_hot,        # 160 dims
        np.zeros(64),   # line_m2
        np.zeros(64),   # line_m1
        line0_emb,      # 64 dims
        np.zeros(64),   # line_1
        np.zeros(64),   # line_2
    ])


def base_entry(issue):
    return {
        "rule":      issue.get("rule", ""),
        "severity":  issue.get("severity", ""),
        "message":   issue.get("message", ""),
        "component": issue.get("component", ""),
        "line":      issue.get("line"),
    }


all_issues = fetch_all_issues()
print(f"[INFO] {len(all_issues)} issues récupérées au total")

true_positives      = []
false_positives     = []
unknown_rule_issues = []
non_java_issues     = []

for issue in all_issues:
    rule = issue.get("rule", "")

    # Règles à forcer en TP quel que soit le préfixe
    if rule in FORCED_TRUE_POSITIVES:
        notes = {
            "secrets:S2068": "Forcé TP : credential hardcodé détecté (secrets)",
            "secrets:S6290": "Forcé TP : credential hardcodé détecté (secrets)",
            "java:S6437":    "Forcé TP : password compromis — révoquer immédiatement",
            "java:S2226":    "Forcé TP : champ mutable dans Servlet partagé → race condition",
            "java:S5122":    "Forcé TP : configuration CORS permissive → risque CSRF",
            "java:S2068":    "Forcé TP : password hardcodé dans le code source Java",
        }
        entry = base_entry(issue)
        entry.update({"ml_prediction": 0, "fp_probability": 0.0,
                      "note": notes.get(rule, "Forcé TP : règle critique")})
        true_positives.append(entry)
        continue

    # Issues non-Java (javascript:, typescript:, secrets: hors liste forcée, etc.)
    if not (rule.startswith("java:") or rule.startswith("squid:")):
        non_java_issues.append(base_entry(issue))
        continue

    # Règle Java inconnue du modèle — stockée pour classification par sévérité
    normalized = normalize_rule(rule)
    if normalized not in squid_list:
        unknown_rule_issues.append(base_entry(issue))
        continue

    # Classification ML
    fv    = build_feature(issue)
    pred  = model.predict([fv])[0]
    proba = model.predict_proba([fv])[0][1]

    entry = base_entry(issue)
    entry.update({"ml_prediction": int(pred), "fp_probability": round(float(proba), 3)})

    if pred == 0:
        true_positives.append(entry)
    else:
        false_positives.append(entry)

# Règles inconnues du modèle : classification de repli par sévérité.
# BLOCKER/CRITICAL → TP forcé ; MAJOR/MINOR/INFO → FP par défaut.
SEVERITY_FORCED_TP = {"BLOCKER", "CRITICAL"}

for issue in unknown_rule_issues:
    if issue["severity"] in SEVERITY_FORCED_TP:
        issue["ml_prediction"]  = 0
        issue["fp_probability"] = 0.0
        issue["note"] = "Forcé TP : règle inconnue du modèle mais sévérité BLOCKER/CRITICAL"
        true_positives.append(issue)
    else:
        issue["ml_prediction"]  = 1
        issue["fp_probability"] = 1.0
        issue["note"] = "Classé FP par défaut : règle inconnue du modèle, sévérité faible"
        false_positives.append(issue)

unknown_tp = sum(1 for i in unknown_rule_issues if i["severity"] in SEVERITY_FORCED_TP)
unknown_fp = len(unknown_rule_issues) - unknown_tp
java_ml_analyzed = len(true_positives) + len(false_positives) - len(unknown_rule_issues)

print(f"[INFO] {java_ml_analyzed} issues Java classées par le modèle ML")
print(f"[INFO] {len(unknown_rule_issues)} issues Java avec règle inconnue → {unknown_tp} TP / {unknown_fp} FP (par sévérité)")
print(f"[INFO] {len(non_java_issues)} issues non-Java ignorées (javascript/secrets/typescript)")

report = {
    "total_issues":                   len(all_issues),
    "java_ml_analyzed":               java_ml_analyzed,
    "unknown_rules_classified":       len(unknown_rule_issues),
    "unknown_rules_forced_tp":        unknown_tp,
    "unknown_rules_default_fp":       unknown_fp,
    "non_java_issues_skipped":        len(non_java_issues),
    "true_positives_count":           len(true_positives),
    "false_positives_count":          len(false_positives),
    "true_positives":                 true_positives,
    "false_positives":                false_positives,
    "non_java_issues":                non_java_issues,
}

with open("sonar_filtered_report.json", "w", encoding="utf-8") as f:
    json.dump(report, f, indent=2, ensure_ascii=False)

print(f"[RÉSULTAT] Vrais positifs : {len(true_positives)}")
print(f"[RÉSULTAT] Faux positifs  : {len(false_positives)}")
print("[RAPPORT] sonar_filtered_report.json généré")
