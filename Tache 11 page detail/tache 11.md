# **OR-376 : Affichage lisible de la colonne “Demande OR”**

## **Objectif**

Permettre à l’administrateur de visualiser immédiatement les informations essentielles d’une demande d’Ordre de Réexpédition (OR) dans la liste admin, sous une forme lisible et structurée, sans afficher le JSON brut.

Cela inclut :

* Type de réexpédition
* Demandeur
* Ancienne / nouvelle adresse
* Dates
* Durée
* Synthèse du contenu OR en phrase simple

---

# 1️ **Modification du DTO : `DemandeOrAdminListDTO`**

Le DTO de la liste admin a été enrichi avec des champs de résumé lisible :

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeOrAdminListDTO {

    private Long id;
    private String statut;
    private Instant createdDate;
    private Instant lastModifiedDate;

    private String typeReexpedition;
    private Instant dateDebut;
    private Instant dateFin;

    private String demandeurNom;
    private String demandeurPrenom;

    private String ancienneVille;
    private String ancienneCodePostal;
    private String nouvelleVille;
    private String nouvelleCodePostal;

    // Résumés lisibles utilisés par le front
    private String resumeType;
    private String resumeAdresses;
    private String resumeDuree;
    private String resumeDates;
}
```

Ce DTO devient la structure principale affichée dans la liste Angular.

---

# 2️ **Lissage des données JSON en DTO lisible**

Dans la méthode backend `listAllForAdmin()` :

### Responsabilité

* Lire le JSON
* Transformer les données
* Générer des résumés humains lisibles

```java
public List<DemandeOrAdminListDTO> listAllForAdmin() {
    return demandeOrService.findAll().stream()
        .map(or -> {
            JsonNode data = or.getData();

            DemandeOrAdminListDTO dto = new DemandeOrAdminListDTO();
            dto.setId(or.getId());
            dto.setStatut(JsonUtils.getStringOrDefault(data, "statut", "INCONNU"));
            dto.setCreatedDate(or.getCreatedDate());
            dto.setLastModifiedDate(or.getLastModifiedDate());

            dto.setTypeReexpedition(JsonUtils.getStringOrDefault(data, "typeReexpedition", null));
            dto.setDateDebut(or.getDateDebut());
            dto.setDateFin(or.getDateFin());

            dto.setDemandeurNom(JsonUtils.getStringOrDefault(data, "demandeurNom", null));
            dto.setDemandeurPrenom(JsonUtils.getStringOrDefault(data, "demandeurPrenom", null));

            JsonNode ancienne = data.path("ancienneAdresse");
            dto.setAncienneVille(JsonUtils.getStringOrDefault(ancienne, "ville", null));
            dto.setAncienneCodePostal(JsonUtils.getStringOrDefault(ancienne, "codePostal", null));

            JsonNode nouvelle = data.path("nouvelleAdresse");
            dto.setNouvelleVille(JsonUtils.getStringOrDefault(nouvelle, "ville", null));
            dto.setNouvelleCodePostal(JsonUtils.getStringOrDefault(nouvelle, "codePostal", null));

            // Construction des résumés affichés côté UI
            dto.setResumeType(buildTypeResume(dto));
            dto.setResumeAdresses(buildAdresseResume(dto));
            dto.setResumeDuree(buildDureeResume(dto));
            dto.setResumeDates(buildDateResume(dto));

            return dto;
        })
        .collect(Collectors.toList());
}
```

---

# 3️ **Construction des résumés lisibles (métier)**

Ces méthodes produisent un texte lisible.

### Résumé Type

```java
private String buildTypeResume(DemandeOrAdminListDTO dto) {
    String type = dto.getTypeReexpedition() != null ? dto.getTypeReexpedition() : "Non spécifié";
    return "Réexpédition : " + type;
}
```

---

### Résumé Adresses

```java
private String buildAdresseResume(DemandeOrAdminListDTO dto) {
    String villeA = dto.getAncienneVille() != null ? dto.getAncienneVille() : "?";
    String villeN = dto.getNouvelleVille() != null ? dto.getNouvelleVille() : "?";
    return "Adresse : " + villeA + " → " + villeN;
}
```

---

### Résumé Durée

```java
private String buildDureeResume(DemandeOrAdminListDTO dto) {
    if(dto.getDateDebut() != null && dto.getDateFin() != null) {
        long days = Duration.between(dto.getDateDebut(), dto.getDateFin()).toDays();
        return "Durée : " + days + " jours";
    }
    return "Durée inconnue";
}
```

---

### Résumé Période

```java
private String buildDateResume(DemandeOrAdminListDTO dto) {
    if(dto.getDateDebut() != null && dto.getDateFin() != null) {
        return "Période : "
            + dto.getDateDebut().toString().substring(0, 10)
            + " → "
            + dto.getDateFin().toString().substring(0, 10);
    }
    return "Période inconnue";
}
```

---

# 4️ **Exposition par endpoint (REST)**

```java
@GetMapping("/or-demandes")
public ResponseEntity<List<DemandeOrAdminListDTO>> listAll() {
    log.debug("REST admin: list all OR demandes");
    return ResponseEntity.ok(orDematService.listAllForAdmin());
}
```

Endpoint :

```
GET /api/demat/or-demandes
```

Ce endpoint renvoie directement les infos déjà formatées.

---

# 5️ **Côté Front (Angular)**

## Type TS aligné sur le DTO backend

```ts
type AdminRow = {
  id: number;
  statut: string;
  createdDate: string;
  lastModifiedDate: string;

  demandeurNom?: string | null;
  demandeurPrenom?: string | null;

  resumeType?: string | null;
  resumeAdresses?: string | null;
  resumeDuree?: string | null;
  resumeDates?: string | null;
};
```

---

# 6️ **Affichage dans le tableau admin (UI)**

```html
<td>
  <div *ngIf="row.demandeurNom || row.demandeurPrenom">
    👤 {{ row.demandeurNom || 'Demandeur' }} {{ row.demandeurPrenom || '' }}
  </div>
  <div *ngIf="row.resumeType">
    🏷️ {{ row.resumeType }}
  </div>
  <div *ngIf="row.resumeAdresses">
    📍 {{ row.resumeAdresses }}
  </div>
  <div *ngIf="row.resumeDuree">
    ⏳ {{ row.resumeDuree }}
  </div>
  <div *ngIf="row.resumeDates">
    📆 {{ row.resumeDates }}
  </div>
</td>
```

✔ Lecture immédiate par l’admin
✔ Données lisibles
✔ Zéro JSON brut affiché

---

# 7️ **Résultat final**

### Avant ❌

```
{
 "nom":"Jean",
 "ville":"Nouméa",
 "statut":"EN_ATTENTE",
 ...
}
```

### Après ✔

```
👤 Jean Dupont
🏷️ Réexpédition : DEFINITIF
📍 Adresse : Nouméa → Dumbéa
⏳ Durée : 30 jours
📆 Période : 2025-11-01 → 2025-12-01
```

