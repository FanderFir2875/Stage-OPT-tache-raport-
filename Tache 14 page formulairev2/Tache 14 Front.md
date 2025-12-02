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

    @NotBlank(message = "La date de début est obligatoire")
    private String dateDebut;

    @NotBlank(message = "La date de fin est obligatoire")
    private String dateFin;

    @NotBlank(message = "Le nom du demandeur est obligatoire")
    private String demandeurNom;

    @NotBlank(message = "Le prénom du demandeur est obligatoire")
    private String demandeurPrenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String demandeurEmail;

    private String demandeurTelephone;

    @NotNull(message = "L’adresse de départ est obligatoire")
    private JsonNode ancienneAdresse;

    @NotNull(message = "L’adresse d’arrivée est obligatoire")
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
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class DemandeOr extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private Instant dateDebut;

    @Column(name = "date_fin", nullable = false)
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

Voici le contenu  :

```java
    public DemandeOr createDemandeOr(OrDematRequestDTO dto) {
        log.info("Création d’une demande d’OR définitif : {}", dto);


        if (dto.getDateDebut() == null || dto.getDateDebut().isBlank())
            throw new IllegalArgumentException("La date de début est obligatoire.");

        if (dto.getDateFin() == null || dto.getDateFin().isBlank())
            throw new IllegalArgumentException("La date de fin est obligatoire.");

        if (dto.getAncienneAdresse() == null)
            throw new IllegalArgumentException("L'adresse de départ est obligatoire.");

        if (dto.getNouvelleAdresse() == null)
            throw new IllegalArgumentException("L'adresse d'arrivée est obligatoire.");


        Instant now = Instant.now();
        Instant debut = Instant.parse(dto.getDateDebut());
        Instant fin = Instant.parse(dto.getDateFin());

        if (debut.isBefore(now)) {
            throw new IllegalArgumentException("La date de début doit être postérieure ou égale à la date du jour." );

        }

        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }

        if (dto.getAncienneAdresse().toString().equals(dto.getNouvelleAdresse().toString())) {
            throw new IllegalArgumentException("L’adresse de départ doit être différente de l’adresse d’arrivée.");
        }

        DemandeOr demandeOr = demandeOrMapper.toEntity(dto);
        DemandeOr saved = demandeOrService.save(demandeOr);
        log.info("Demande d’OR définitif sauvegardée avec succès - ID: {}", saved.getId());
        return saved;
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
    "message": "Votre demande d’Ordre de Réexpédition a bien été enregistrée."
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
  "dateDebut": "2025-12-05T00:00:00Z",
  "dateFin": "2026-05-01T00:00:00Z",
  "demandeurNom": "Dupont",
  "demandeurPrenom": "Pierre",
  "demandeurEmail": "pierre@example.com",
  "demandeurTelephone": "505050",
  "ancienneAdresse": {
    "numEtVoie": "12 rue Martin",
    "ville": "Nouméa",
    "codePostal": "98800"
  },
  "nouvelleAdresse": {
    "numEtVoie": "18 avenue Foch",
    "ville": "Dumbéa",
    "codePostal": "98835"
  }
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



