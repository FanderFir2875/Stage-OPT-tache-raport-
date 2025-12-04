# Documentation technique – Backend OR Physique → Physique

##  Objectif

Implémenter la logique serveur permettant :

* de recevoir une demande OR en JSON depuis le frontend
* de valider les données
* de vérifier les règles métiers
* de transformer les données en entité persistable
* de stocker la demande en base
* de retourner une réponse HTTP cohérente

---

# Représentation des données reçues (DTO d’entrée)

Nous avons créé `OrDematRequestDTO`, qui représente le JSON reçu depuis le frontend :

```java
@Data
public class OrDematRequestDTO {

    private String dateDebut;
    private String dateFin;

    private String demandeurNom;
    private String demandeurPrenom;
    private String demandeurEmail;
    private String demandeurTelephone;

    private JsonNode ancienneAdresse;
    private JsonNode nouvelleAdresse;
}
```

Ce DTO ne correspond pas 1:1 au modèle BDD, mais au **format envoyé par le frontend**.

---

# Entité persistée en base

Notre modèle stocké en BDD est `DemandeOr`

```java
@Entity
@Table(name = "demande_or")
public class DemandeOr extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(...)
    private Long id;

    private Instant dateDebut;
    private Instant dateFin;

    @Type(type = "jsonb")
    @Column(name = "data", columnDefinition = "TEXT", nullable = false)
    private JsonNode data;
}
```

Important :
Toutes les infos client (nom, adresse, tel, etc.) sont stockées dans `data` (format JSON-B).

---

# Validation métier dans le service

La logique métier se trouve dans `OrDematService.createDemandeOr()`.

Voici le contenu essentiel :

```java
public DemandeOr createDemandeOr(OrDematRequestDTO dto) {

    if (dto.getDateDebut() == null)
        throw new IllegalArgumentException("La date de début est obligatoire.");
    if (dto.getDateFin() == null)
        throw new IllegalArgumentException("La date de fin est obligatoire.");
    if (dto.getAncienneAdresse() == null)
        throw new IllegalArgumentException("L'adresse de départ est obligatoire.");
    if (dto.getNouvelleAdresse() == null)
        throw new IllegalArgumentException("L'adresse d'arrivée est obligatoire.");

    LocalDate debut = Instant.parse(dto.getDateDebut()).atZone(ZoneId.of("Pacific/Noumea")).toLocalDate();
    LocalDate now = LocalDate.now(ZoneId.of("Pacific/Noumea"));

    if (debut.isBefore(now))
        throw new IllegalArgumentException("La date de début doit être postérieure ou égale à la date du jour.");

    if (dto.getAncienneAdresse().toString().equals(dto.getNouvelleAdresse().toString()))
        throw new IllegalArgumentException("L’adresse de départ doit être différente de l’adresse d’arrivée.");

    DemandeOr demandeOr = demandeOrMapper.toEntity(dto);
    return demandeOrService.save(demandeOr);
}
```

Nous appliquons les règles métier suivantes :

✔ Champs obligatoires
✔ Dates valides
✔ Adresse départ ≠ Adresse arrivée
✔ Date début ≥ date du jour

---

# Construction de la structure JSON stockée en base

Le mapper MapStruct convertit :

```java
public abstract DemandeOr toEntity(OrDematRequestDTO dto);
```

Pour créer l'entité DemandeOr :

* dateDebut → `Instant`
* dateFin → `Instant`
* toutes les autres infos → regroupées dans `data`

Cela donne un stockage BDD du type :

```json
{
  "demandeurNom": "Dupont",
  "demandeurPrenom": "Pierre",
  "demandeurEmail": "pierre@example.com",
  "demandeurTelephone": "505050",
  "ancienneAdresse": { ... },
  "nouvelleAdresse": { ... }
}
```

---

# Sauvegarde en base

Effectuée via :

```java
DemandeOr saved = demandeOrService.save(demandeOr);
```

qui appelle le repository :

```java
demandeOrRepository.save(demandeOr);
```

---

# Réponse HTTP envoyée au frontend

En cas de succès :

```
201 CREATED
```

Exemple de retour :

```json
{
  "id": 42,
  "message": "Ordre de réexpédition créé avec succès"
}
```

En cas d’erreur :

```
400 BAD REQUEST
```

Exemple :

```json
{
  "message": "L’adresse de départ doit être différente de l’adresse d’arrivée."
}
```

---

# POSTMAN example

```
POST /api/demat/or-physique
Content-Type: application/json
```

```json
{
  "dateDebut": "2025-05-12T00:00:00Z",
  "dateFin": "2026-05-01T00:00:00Z",
  "demandeurNom": "Dupont",
  "demandeurPrenom": "Pierre",
  "demandeurEmail": "pierre@example.com",
  "demandeurTelephone": "505050",
  "ancienneAdresse": { ... },
  "nouvelleAdresse": { ... }
}
```

---

# Fonctionnalités backend finalisées

## Implémentées :

✔ validation obligatoire des champs
✔ validation adresse différente
✔ validation date début ≥ date du jour
✔ validation date fin ≥ date début
✔ stockage JSONB
✔ logs lisibles et détaillés
✔ gestion des exceptions REST
✔ endpoint fonctionnel

---

# Ce qu’il reste à faire (prochaines étapes FRONT)

* ajout autocomplete REFloc sur champs adresse
* affichage des messages d’erreur sous les champs
* blocage du bouton tant que formulaire invalide
* formatage des dates vers `Instant`
* affichage du message succès validation

---



