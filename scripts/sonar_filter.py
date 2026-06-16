import requests
import joblib
import numpy as np
import json
import os
import sys
import types
from gensim.models import Word2Vec

# The w2v model was pickled in an env that had a 'tools' package (including
# tools.callbacks). Stub out the entire tools.* namespace with an import hook
# so pickle can resolve any submodule reference without crashing.
class _StubFinder:
    def find_module(self, name, path=None):
        if name == "tools" or name.startswith("tools."):
            return self

    def load_module(self, name):
        if name not in sys.modules:
            mod = types.ModuleType(name)
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


def embed_text(text):
    if not text:
        return np.zeros(64)
    tokens = text.lower().split()
    vecs = [w2v.wv[t] for t in tokens if t in w2v.wv]
    if not vecs:
        return np.zeros(64)
    return np.mean(vecs, axis=0)


def build_feature(issue):
    raw_rule = issue.get("rule", "")
    normalized = raw_rule.replace(":", "_")

    one_hot = np.zeros(160)
    if normalized in squid_list:
        one_hot[squid_list.index(normalized)] = 1

    line0_emb = embed_text(issue.get("message", ""))

    feature = np.concatenate([
        one_hot,          # 160 dims
        np.zeros(64),     # line_m2
        np.zeros(64),     # line_m1
        line0_emb,        # 64 dims
        np.zeros(64),     # line_1
        np.zeros(64),     # line_2
    ])
    return feature


issues = fetch_all_issues()
print(f"[INFO] {len(issues)} issues récupérées depuis SonarCloud")

true_positives = []
false_positives = []

for issue in issues:
    fv = build_feature(issue)
    pred  = model.predict([fv])[0]
    proba = model.predict_proba([fv])[0][1]

    entry = {
        "rule":          issue.get("rule", ""),
        "severity":      issue.get("severity", ""),
        "message":       issue.get("message", ""),
        "component":     issue.get("component", ""),
        "line":          issue.get("line"),
        "ml_prediction": int(pred),
        "fp_probability": round(float(proba), 3),
    }

    if pred == 0:
        true_positives.append(entry)
    else:
        false_positives.append(entry)

report = {
    "total_issues":        len(issues),
    "true_positives_count":  len(true_positives),
    "false_positives_count": len(false_positives),
    "true_positives":      true_positives,
    "false_positives":     false_positives,
}

with open("sonar_filtered_report.json", "w", encoding="utf-8") as f:
    json.dump(report, f, indent=2, ensure_ascii=False)

print(f"[RÉSULTAT] Vrais positifs (bugs réels) : {len(true_positives)}")
print(f"[RÉSULTAT] Faux positifs (fausses alarmes) : {len(false_positives)}")
print("[RAPPORT] sonar_filtered_report.json généré")
