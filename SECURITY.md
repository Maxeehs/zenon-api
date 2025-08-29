# Politique de sécurité

Merci d’aider à sécuriser <NOM_DU_PROJET> en nous signalant toute vulnérabilité de manière responsable.

## 📦 Périmètre
Cette politique couvre :
- Le code source de ce dépôt.
- Les workflows GitHub Actions liés.
- Les configurations de déploiement documentées dans ce repo.

Sont hors périmètre : services tiers, extensions/nœuds externes, et tout composant non maintenu ici.

## 🧭 Versions prises en charge
Les correctifs de sécurité sont fournis pour les branches encore maintenues :
- `main` (support actif)
- Tags/releases publiés au cours des **12 derniers mois** ou marqués “Latest”.

Les anciennes versions peuvent recevoir des mitigations, sans garantie de patch.

## 🔒 Signaler une vulnérabilité (canal privé)
**Préféré :** via GitHub “Private vulnerability reporting”  
➡️ *Repo* → **Security** → **Advisories** → **Report a vulnerability**

**Alternatif :** e-mail à **security@<ton-domaine>** (ou **<ton-email>**)  
- (Optionnel) Chiffrez votre message avec notre clé PGP : **<empreinte_PGP>** (téléchargement : `<URL_CLE_PGP>`)

Merci d’éviter les issues publiques pour les vulnérabilités.

## ✅ Directives de divulgation responsable
- Accordez-nous un délai raisonnable pour analyser et publier un correctif **(objectif : accusé de réception ≤ 3 jours ouvrés, évaluation initiale ≤ 7 jours, correctif ou plan ≤ 30 jours)**.
- Évitez tout accès, modification ou exfiltration de données d’autrui.
- Limitez vos tests : pas d’attaque DDoS, pas de spam, pas d’ingénierie sociale, pas d’altération durable d’infra.
- Fournissez un **PoC minimal**, reproductible, non destructif, et — si possible — un scénario d’exploitation réaliste.

## 🧾 Informations utiles à fournir
- Version/commit impacté, environnement (local, CI, prod).
- Type de vulnérabilité (ex. RCE, XSS, SSRF, IDOR, CSRF, vulnérabilité supply chain…).
- Étapes de reproduction, PoC, impact, portée, prérequis.
- Idées de remédiation/patch si vous en avez.

## 🪪 Safe Harbor
Si vous respectez cette politique :
- Nous ne poursuivrons pas de démarches légales contre les recherches de bonne foi.
- Nous considérerons la recherche comme autorisée, tant qu’elle reste proportionnée et non destructive.
- Ce Safe Harbor ne couvre pas les actions illégales ni les violations de droits de tiers.

## 🧰 Classement de sévérité
Nous évaluons avec **CVSS v3.1** (ou v4.0 si applicable) et classons : Critique / Élevée / Modérée / Faible.

## 🛠️ Processus de correction
1. Accusé de réception et attribution d’un identifiant interne.
2. Reproduction, évaluation de l’impact, score CVSS.
3. Développement du correctif + tests + revue.
4. Publication d’une **release patch** et d’un **advisory** (CVE si éligible).
5. Communication de crédits au chercheur (opt-in).

## 🏅 Crédits & bug bounty
- Pas de programme de bug bounty rémunéré pour le moment.
- Nous créditons volontiers les chercheurs (nom/pseudo) dans l’advisory et les notes de version, **avec votre accord**.

## 🔗 Dépendances
- Nous suivons les alertes Dependabot/Security.
- Les vulnérabilités dans des dépendances upstream sont généralement redirigées vers les mainteneurs d’origine ; nous publions des mises à jour dès que disponibles.

## 🚫 Non éligible (exemples)
- Problèmes purement théoriques sans impact exploitable.
- Bonnes pratiques manquantes sans scénario d’attaque clair (ex. en-têtes HTTP absents sur un site statique non sensible).
- Version explicitement non supportée.
- Self-XSS, clickjacking sans contexte sensible, enumeration non sensible, info-leaks à faible impact sans données privées.

## 🧭 Contact d’urgence
En cas de risque critique et largement exploité : **security@<ton-domaine>** (objet “URGENT”).

---

_Optionnel mais recommandé en prod : publier un `/.well-known/security.txt` pointant vers ce document et votre contact sécurité._
