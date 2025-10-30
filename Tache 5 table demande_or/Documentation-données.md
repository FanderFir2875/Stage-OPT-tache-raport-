Parfait 👍
Voici la section **“Documentation Confluence — Traçabilité des données `DemandeOr`”**, prête à copier dans Confluence (présentée comme un article clair et complet, formaté comme le veut JHipster/SIOR).

---

# 🧾 Documentation Confluence — Traçabilité des données : `DemandeOr`

## 🎯 Objectif

Cette documentation décrit la **traçabilité complète des données** pour la nouvelle table **`demande_or`** introduite dans SIOR.
Elle permet de comprendre **où et comment les données sont créées, transformées, stockées et auditées** au sein du système.

---

## 📊 Donnée principale : `DemandeOr`

### Description fonctionnelle

La table `demande_or` a pour but de **stocker les informations de base d’une demande d’ordre de réexpédition dématérialisée (OR)**.
Chaque enregistrement correspond à **une demande client** comportant :

* une **date de début** (`date_debut`)
* une **date de fin** (`date_fin`)
* des **informations d’audit** (création / modification).

---

## 🧱 Traçabilité technique complète

| Étape | Couche                | Élément concerné                     | Description                                                              |
| ----- | --------------------- | ------------------------------------ | ------------------------------------------------------------------------ |
| 1️⃣   | **Frontend**          | (Formulaire Vue/Angular)             | L’utilisateur saisit les dates de début et de fin dans l’IHM             |
| 2️⃣   | **Controller (REST)** | `OrDematResource`                    | Reçoit le JSON (DTO `OrDematDTO`) et le transmet au service              |
| 3️⃣   | **Service métier**    | `OrDematService`                     | Valide les données, vérifie la cohérence (format / ordre des dates)      |
| 4️⃣   | **Mapper**            | `DemandeOrMapper`                    | Convertit le DTO en entité `DemandeOr` (MapStruct)                       |
| 5️⃣   | **Repository (JPA)**  | `DemandeOrRepository`                | Persiste l’entité dans la table PostgreSQL `demande_or`                  |
| 6️⃣   | **Base de données**   | Table `demande_or`                   | Contient les colonnes `date_debut`, `date_fin`, `created_by`, etc.       |
| 7️⃣   | **Auditing JHipster** | `AbstractAuditingEntity`             | Enrichit automatiquement les champs `createdBy`, `createdDate`, etc.     |
| 8️⃣   | **Logs applicatifs**  | `OrDematResource` + `OrDematService` | Traçabilité des appels REST et des validations dans les logs applicatifs |

---

## 🧩 Détails du modèle de données

### Schéma SQL (Liquibase)

```sql
CREATE TABLE demande_or (
    id BIGSERIAL PRIMARY KEY,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date TIMESTAMP
);
```

---

## 🧠 Audit et traçabilité applicative

L’auditing est **hérité automatiquement** de `AbstractAuditingEntity`, qui est déjà intégrée dans l’architecture SIOR.

### Champs ajoutés automatiquement :

| Champ                | Type          | Rôle                               |
| -------------------- | ------------- | ---------------------------------- |
| `created_by`         | `VARCHAR(50)` | Utilisateur qui a créé la demande  |
| `created_date`       | `TIMESTAMP`   | Date de création                   |
| `last_modified_by`   | `VARCHAR(50)` | Utilisateur ayant modifié l’entrée |
| `last_modified_date` | `TIMESTAMP`   | Date de la dernière modification   |

Ces champs sont automatiquement remplis :

* lors de la création (au `save()` JPA)
* lors de la mise à jour (via l’intercepteur d’audit Spring Data)

---

## 🧾 Exemple de traçabilité complète (du front à la base)

### Exemple d’appel HTTP :

```http
POST /api/or-demat
Content-Type: application/json

{
  "dateDebut": "2025-02-01",
  "dateFin": "2025-03-01"
}
```

### Étapes techniques :

| Étape | Composant                | Action                                                          |
| ----- | ------------------------ | --------------------------------------------------------------- |
| ①     | `OrDematResource`        | Reçoit la requête JSON                                          |
| ②     | `OrDematService`         | Valide les dates et crée l’objet `DemandeOr`                    |
| ③     | `DemandeOrMapper`        | Convertit le DTO → Entité JPA                                   |
| ④     | `DemandeOrRepository`    | Sauvegarde l’entité en base                                     |
| ⑤     | PostgreSQL               | Insère une ligne dans `demande_or`                              |
| ⑥     | `AbstractAuditingEntity` | Ajoute automatiquement les infos `created_by` et `created_date` |
| ⑦     | Réponse HTTP             | Retourne un message JSON de confirmation                        |

---

### Exemple de ligne enregistrée dans la base :

| id | date_debut          | date_fin            | created_by | created_date        | last_modified_by | last_modified_date |
| -- | ------------------- | ------------------- | ---------- | ------------------- | ---------------- | ------------------ |
| 1  | 2025-02-01 00:00:00 | 2025-03-01 00:00:00 | `adminf`   | 2025-10-30 08:32:15 | *(null)*         | *(null)*           |

---

## 🔍 Points de contrôle pour la traçabilité

| Contrôle                   | Moyen de vérification                              | Emplacement                              |
| -------------------------- | -------------------------------------------------- | ---------------------------------------- |
| ✅ Validation des dates     | Logs + test OrDematResourceIT                      | `nc.opt.sior.web.rest.OrDematResourceIT` |
| ✅ Persistance effective    | Vérification en base (table `demande_or`)          | PostgreSQL / DBeaver                     |
| ✅ Auditing actif           | Présence des colonnes `created_by`, `created_date` | `AbstractAuditingEntity`                 |
| ✅ Traçabilité front → back | Logs applicatifs + tests d’intégration             | `/api/or-demat`                          |

---

## 🧰 Sources techniques

| Élément        | Chemin du fichier                                                                      |
| -------------- | -------------------------------------------------------------------------------------- |
| **Entité JPA** | `src/main/java/nc/opt/sior/domain/DemandeOr.java`                                      |
| **Liquibase**  | `src/main/resources/config/liquibase/changelog/20251029-01_added_table_demande_or.xml` |
| **Repository** | `src/main/java/nc/opt/sior/repository/DemandeOrRepository.java`                        |
| **Service**    | `src/main/java/nc/opt/sior/service/OrDematService.java`                                |
| **Controller** | `src/main/java/nc/opt/sior/web/rest/OrDematResource.java`                              |
| **Tests IT**   | `src/test/java/nc/opt/sior/web/rest/OrDematResourceIT.java`                            |

---

## 📋 Conclusion

Cette documentation garantit la **traçabilité complète** du flux de données de la table `demande_or` :

* ✅ Données **validées**, **persistées**, et **auditables**
* ✅ Conformes à la structure standard JHipster (Repository / Service / Resource)
* ✅ Couvertes par des **tests automatisés**

---

Souhaites-tu que je te fasse suivre directement la **“Annexe pédagogique : Explication des couches Spring Boot et JPA”** juste après cette partie (comme suite logique pour Confluence) ?
