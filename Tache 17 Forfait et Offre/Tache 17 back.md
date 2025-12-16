
---

# Implémentation BACK-END – Forfaits & Offres

## OR dématérialisé (OR définitif)

---

## Objectif de la tâche

Mettre en place la **gestion des Offres et Forfaits** pour les **Ordres de Réexpédition définitifs créés en ligne**, afin de :

* Supprimer la saisie manuelle de la **date de fin**
* Calculer automatiquement la durée du contrat à partir du **forfait**
* Associer correctement **Offre + Forfait** :

  * à la **Demande OR**
  * puis au **Contrat**
* Garantir la cohérence métier (OR définitif uniquement)

---

## Règles métier implémentées

### OR définitif uniquement

* Pas d’OR temporaire
* Pas de renouvellement

### Forfaits autorisés

* 6 mois
* 12 mois
* 3 mois

### Validation Offre / Forfait

* Le forfait doit appartenir à l’offre **OR-D**
* Toute autre offre est refusée

---

## Évolution des DTO

### Avant

```java
private String dateFin;
```

### Maintenant

* `dateFin` **n’est plus fournie par le client**
* Elle est **calculée côté backend**
* Le front n’envoie **que `dateDebut` + `forfaitId`**

---

### DTO d’entrée – `OrDematRequestDTO`

```java
@Data
public class OrDematRequestDTO {

    @NotBlank(message = "La date de début est obligatoire")
    private String dateDebut;

    @NotNull(message = "Le forfait est obligatoire")
    private Long forfaitId;

    @NotBlank
    private String demandeurNom;

    @NotBlank
    private String demandeurPrenom;

    @Email
    private String demandeurEmail;

    private String demandeurTelephone;

    @NotNull
    private JsonNode ancienneAdresse;

    @NotNull
    private JsonNode nouvelleAdresse;
}
```

**Aucune date de fin ici**

---

### DTO de réponse – `OrDematResponseDTO`

```java
@Data
public class OrDematResponseDTO {
    private Long id;
    private String dateDebut;
    private String dateFin;
    private String message;
}
```

`dateFin` est **renvoyée par le backend**, jamais envoyée par le client

---

## Mapper – `DemandeOrMapper`

### Responsabilités

* Mapper le DTO vers l’entité
* Construire le JSON `data`
* Laisser `dateDebut` / `dateFin` être définies par le service

### Points clés

```java
@Mapper(componentModel = "spring")
public abstract class DemandeOrMapper {

    @Mapping(target = "dateDebut", ignore = true)
    @Mapping(target = "dateFin", ignore = true)
    @Mapping(target = "data", expression = "java(buildDataJson(dto))")
    public abstract DemandeOr toEntity(OrDematRequestDTO dto);

    @Mapping(target = "dateDebut", expression = "java(toIsoString(entity.getDateDebut()))")
    @Mapping(target = "dateFin", expression = "java(toIsoString(entity.getDateFin()))")
    @Mapping(target = "message", constant = "Ordre de réexpédition créé avec succès")
    public abstract OrDematResponseDTO toDto(DemandeOr entity);
}
```

---

## Service principal – `OrDematService`

### Rôle du service

* Validation métier
* Calcul des dates
* Enrichissement du JSON
* Orchestration de la création du contrat

---

### Étapes implémentées

1. Validation du DTO
2. Parsing de `dateDebut`
3. Chargement du **forfait**
4. Validation **offre OR-D**
5. Validation **durée (6 ou 12 mois)**
6. Calcul automatique de `dateFin`
7. Enrichissement du champ `data`
8. Sauvegarde de la demande
9. Création du contrat

---

### Extrait clé – Calcul des dates

```java
LocalDate dateDebutLd = LocalDate.parse(dto.getDateDebut());

LocalDate dateFinLd = dateDebutLd.plusMonths(forfait.getDuree());

Instant debut = dateDebutLd
    .atStartOfDay(ZoneId.systemDefault())
    .toInstant();

Instant fin = dateFinLd
    .atStartOfDay(ZoneId.systemDefault())
    .toInstant();
```

---

### Validation du forfait

```java
Forfait forfait = forfaitRepository.findById(dto.getForfaitId())
    .orElseThrow(() -> new IllegalArgumentException("Forfait invalide"));

Offre offre = forfait.getOffre();
if (offre == null || !"OR-D".equals(offre.getCode())) {
    throw new IllegalArgumentException("Forfait non autorisé pour OR définitif");
}

if (forfait.getDuree() != 6 && forfait.getDuree() != 12) {
    throw new IllegalArgumentException("Durée de forfait non autorisée");
}
```

---

## Enrichissement du JSON `data` (Demande OR)

Le champ `data` contient maintenant :

```json
{
  "demandeurNom": "DUPONT",
  "demandeurPrenom": "Alexandre",
  "demandeurEmail": "alex@test.nc",

  "offre": {
    "id": 2,
    "code": "OR-D",
    "libelle": "OR Définitif"
  },

  "forfait": {
    "id": 8,
    "libelle": "Intérieur - 6 mois",
    "espace": "INTERIEUR",
    "duree": 6,
    "prix": 1200
  }
}
```

Sert de **trace fonctionnelle**, pas de source métier principale

---

## Création du contrat – `OrDematContratService`

### Principe clé

**Le contrat ne lit pas le JSON pour afficher les données**

Il recharge le **Forfait depuis la base** pour garantir :

* cohérence métier
* relations JPA valides
* affichage admin correct

---

### Implémentation

```java
Long forfaitId = data.path("forfait").path("id").asLong(0);

Forfait forfait = forfaitRepository.findById(forfaitId)
    .orElseThrow(() ->
        new IllegalStateException("Forfait introuvable en base : " + forfaitId)
    );

contrat.setForfait(forfait);
```

---

## Sauvegarde finale

Le contrat est enregistré via :

```java
contratService.saveContrat(ContratCompletDTO)
```

Ce qui garantit :

* `contrat.forfait` présent
* `contrat.forfait.offre` présent
* affichage correct côté admin

---

## Résultat backend final

- `dateFin` calculée automatiquement
- `dateFin` absente du DTO d’entrée
- Offre & Forfait validés métier
- Données persistées proprement
- Contrat admin exploitable sans JSON brut

---

