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


def fetch_all_issues():
    issues = []
    page = 1
    total = None
    while True:
        resp = requests.get(
            f"{SONAR_URL}/api/issues/search",
            params={"componentKeys": PROJECT_KEY, "languages": "java", "ps": 500, "p": page},
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


def is_java_rule(rule):
    return rule.startswith("java:") or rule.startswith("squid:")


all_issues = fetch_all_issues()
print(f"[INFO] {len(all_issues)} issues récupérées au total")

true_positives   = []
false_positives  = []
unknown_rule_issues = []
non_java_issues  = []

for issue in all_issues:
    rule = issue.get("rule", "")

    if not is_java_rule(rule):
        non_java_issues.append({
            "rule":      rule,
            "severity":  issue.get("severity", ""),
            "message":   issue.get("message", ""),
            "component": issue.get("component", ""),
            "line":      issue.get("line"),
        })
        continue

    normalized = normalize_rule(rule)
    if normalized not in squid_list:
        unknown_rule_issues.append({
            "rule":      rule,
            "severity":  issue.get("severity", ""),
            "message":   issue.get("message", ""),
            "component": issue.get("component", ""),
            "line":      issue.get("line"),
        })
        continue

    fv    = build_feature(issue)
    pred  = model.predict([fv])[0]
    proba = model.predict_proba([fv])[0][1]

    entry = {
        "rule":           rule,
        "severity":       issue.get("severity", ""),
        "message":        issue.get("message", ""),
        "component":      issue.get("component", ""),
        "line":           issue.get("line"),
        "ml_prediction":  int(pred),
        "fp_probability": round(float(proba), 3),
    }

    if pred == 0:
        true_positives.append(entry)
    else:
        false_positives.append(entry)

java_analyzed = len(true_positives) + len(false_positives)
print(f"[INFO] {java_analyzed} issues Java analysées par le modèle ML")
print(f"[INFO] {len(non_java_issues)} issues non-Java ignorées (javascript/secrets/typescript)")

report = {
    "total_issues":            len(all_issues),
    "java_issues_analyzed":    java_analyzed,
    "non_java_issues_skipped": len(non_java_issues),
    "true_positives_count":    len(true_positives),
    "false_positives_count":   len(false_positives),
    "true_positives":          true_positives,
    "false_positives":         false_positives,
    "non_java_issues":         non_java_issues,
}

with open("sonar_filtered_report.json", "w", encoding="utf-8") as f:
    json.dump(report, f, indent=2, ensure_ascii=False)

print(f"[RÉSULTAT] Vrais positifs Java : {len(true_positives)}")
print(f"[RÉSULTAT] Faux positifs Java  : {len(false_positives)}")
print("[RAPPORT] sonar_filtered_report.json généré")
