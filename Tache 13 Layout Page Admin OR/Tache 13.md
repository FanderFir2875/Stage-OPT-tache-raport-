

# OR-376 – Amélioration de l’affichage de la colonne “Demande OR” (Admin)

## 🎯 Objectif de la tâche

En tant qu’administrateur, je veux que la colonne **“Demande OR”** soit lisible et synthétique, afin de comprendre rapidement le contenu d’une demande d’Ordre de Réexpédition **sans** devoir lire du JSON brut.

Avant la tâche :

* Le JSON complet de la demande OR était stocké dans `demande_or.data` (JSONB).
* L’admin voyait soit :

  * du JSON brut, soit
  * un affichage très pauvre / difficile à lire.

Après la tâche :

* L’admin voit un **bloc d’information structuré**, avec :

  * le demandeur
  * le type de réexpédition
  * l’ancienne / la nouvelle adresse
  * la durée et la période de réexpédition
* Plus aucun JSON brut n’est affiché dans la liste.

---

## ✅ Critères d’acceptation (rappel)

1. **Plus de JSON brut**

   * La colonne “Demande OR” n’affiche plus le contenu de `data` en JSON.
   * Les données OR sont présentées sous une forme **lisible** (texte + labels).

2. **Informations clés visibles**

   * Type de réexpédition
   * Durée / période
   * Ancienne / nouvelle adresse
   * Personne concernée (ou au moins un résumé clair)

---

## 🧱 Vue d’ensemble de la solution

### Côté backend

* Ajout de champs de résumé **structuré** dans le DTO `DemandeOrAdminListDTO` :

  * `resumeType`
  * `resumeAdresses`
  * `resumeDuree`
  * `resumeDates`
* Complétion du service `OrDematService.listAllForAdmin()` pour :

  * lire les données JSON
  * remplir les champs “simples” (nom, villes, dates…)
  * construire les **blocs de résumé lisibles** via des méthodes dédiées :

    * `buildTypeResume(...)`
    * `buildAdresseResume(...)`
    * `buildDureeResume(...)`
    * `buildDateResume(...)`

### Côté frontend (Angular)

* Le composant `ListeOrAdminComponent` récupère la liste via :

  ```ts
  GET /api/demat/or-demandes
  ```
* Le type `AdminRow` est aligné avec le DTO backend (les mêmes champs).
* Le template HTML affiche **un bloc multi-ligne** par demande OR, du style :

  ```text
  👤 Jean Dupont
  🏷️ Réexpédition : DEFINITIF
  📍 Adresse : Nouméa → Dumbéa
  ⏳ Durée : 30 jours
  📆 Période : 2025-11-01 → 2025-12-01
  ```

---

## 1️⃣ Backend – DTO & service

### 1.1. DTO `DemandeOrAdminListDTO`

On part d’un DTO “liste admin” qui contenait déjà les infos de base :

```java
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

    // Champs ajoutés pour l’affichage structuré
    private String resumeType;
    private String resumeAdresses;
    private String resumeDuree;
    private String resumeDates;
}
```

> Objectif : le front n’a **pas** à recomposer ces résumés.
> Tout est préparé côté backend.

---

### 1.2. Méthode `listAllForAdmin()`

Cette méthode :

1. Récupère toutes les entités `DemandeOr` via `demandeOrService.findAll()`.
2. Pour chaque entité, lit le JSON `data`.
3. Crée un `DemandeOrAdminListDTO`.
4. Remplit les champs simples (nom, villes, dates, type…).
5. Construit les champs de résumé avec des méthodes dédiées.

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

            // Construction des résumés lisibles
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

### 1.3. Méthodes de construction des résumés

#### Type de réexpédition

```java
private String buildTypeResume(DemandeOrAdminListDTO dto) {
    String type = dto.getTypeReexpedition() != null ? dto.getTypeReexpedition() : "Non spécifié";
    return "Réexpédition : " + type;
}
```

#### Adresses

```java
private String buildAdresseResume(DemandeOrAdminListDTO dto) {
    String villeA = dto.getAncienneVille() != null ? dto.getAncienneVille() : "?";
    String villeN = dto.getNouvelleVille() != null ? dto.getNouvelleVille() : "?";
    return "Adresse : " + villeA + " → " + villeN;
}
```

#### Durée (en jours)

```java
private String buildDureeResume(DemandeOrAdminListDTO dto) {
    if (dto.getDateDebut() != null && dto.getDateFin() != null) {
        long days = Duration.between(dto.getDateDebut(), dto.getDateFin()).toDays();
        return "Durée : " + days + " jours";
    }
    return "Durée inconnue";
}
```

#### Période

```java
private String buildDateResume(DemandeOrAdminListDTO dto) {
    if (dto.getDateDebut() != null && dto.getDateFin() != null) {
        return "Période : "
            + dto.getDateDebut().toString().substring(0, 10)
            + " → "
            + dto.getDateFin().toString().substring(0, 10);
    }
    return "Période inconnue";
}
```

---

## 2️⃣ Backend – Endpoint REST admin

Le contrôleur expose la liste admin via :

```java
@GetMapping("/or-demandes")
public ResponseEntity<List<DemandeOrAdminListDTO>> listAll() {
    log.debug("REST admin: list all OR demandes");
    return ResponseEntity.ok(orDematService.listAllForAdmin());
}
```

Consommé par le front :

```http
GET /api/demat/or-demandes
```

La réponse contient **déjà** les champs de résumé :

```json
{
  "id": 41,
  "statut": "EN_ATTENTE",
  "demandeurNom": "Jean",
  "demandeurPrenom": "Dupont",
  "ancienneVille": "Nouméa",
  "nouvelleVille": "Dumbéa",
  "resumeType": "Réexpédition : DEFINITIF",
  "resumeAdresses": "Adresse : Nouméa → Dumbéa",
  "resumeDuree": "Durée : 30 jours",
  "resumeDates": "Période : 2025-11-01 → 2025-12-01"
}
```

---

## 3️⃣ Frontend – TypeScript & template

### 3.1. Type `AdminRow`

Dans `liste-or-admin.component.ts` :

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

@Component({
  selector: 'app-liste-or-admin',
  templateUrl: './liste-or-admin.component.html',
})
export class ListeOrAdminComponent implements OnInit {
  data: AdminRow[] = [];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<AdminRow[]>('/api/demat/or-demandes').subscribe({
      next: rows => (this.data = rows),
      error: err => console.error('Erreur chargement demandes OR admin', err),
    });
  }

  voirDetail(id: number): void {
    this.router.navigate(['/admin/or', id]);
  }
}
```

> Le front **ne reconstruit plus** la logique métier, il se contente d’afficher ce que le backend lui donne.

---

### 3.2. Template HTML (option C : affichage bloc riche)

Dans `liste-or-admin.component.html` :

```html
<h1 jhiTranslate="sior.listeOrAdmin.title">Demandes OR (Admin)</h1>

<table class="table table-striped" *ngIf="data.length > 0">
  <thead>
    <tr>
      <th jhiTranslate="sior.listeOrAdmin.columns.id">Numéro de demande</th>
      <th jhiTranslate="sior.listeOrAdmin.columns.resume">Demande OR</th>
      <th jhiTranslate="sior.listeOrAdmin.columns.statut">Statut</th>
      <th jhiTranslate="sior.listeOrAdmin.columns.createdDate">Date de création</th>
      <th jhiTranslate="sior.listeOrAdmin.columns.lastModifiedDate">Dernière mise à jour</th>
      <th></th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let row of data">
      <td>{{ row.id }}</td>

      <!-- Bloc lisible -->
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

      <td>
        <span class="badge bg-warning" *ngIf="row.statut === 'EN_ATTENTE'">En attente</span>
        <span class="badge bg-success" *ngIf="row.statut === 'VALIDE'">Validé</span>
        <span class="badge bg-danger" *ngIf="row.statut === 'REFUSE'">Refusé</span>
      </td>

      <td>{{ row.createdDate | date: 'short' }}</td>
      <td>{{ row.lastModifiedDate | date: 'short' }}</td>

      <td>
        <button class="btn btn-sm btn-outline-primary" (click)="voirDetail(row.id)">
          Voir détail
        </button>
      </td>
    </tr>
  </tbody>
</table>

<p *ngIf="data.length === 0" jhiTranslate="sior.listeOrAdmin.noData"></p>
```

---

## 4️⃣ Tests & vérifications

### 4.1. Donnée de test SQL

Exemple d’insertion pour tester un cas complet :

```sql
INSERT INTO demande_or (
    id,
    date_debut,
    date_fin,
    created_by,
    created_date,
    last_modified_by,
    last_modified_date,
    data
) VALUES (
    41,
    '2025-11-01',
    '2025-12-01',
    'test_user',
    NOW(),
    'test_user',
    NOW(),
    '{
        "statut": "EN_ATTENTE",
        "demandeurNom": "Jean",
        "demandeurPrenom": "Dupont",
        "demandeurEmail": "jean.dupont@email.com",
        "demandeurTelephone": "778899",
        "typeReexpedition": "DEFINITIF",
        "ancienneAdresse": { "ville": "Nouméa", "codePostal": "98800" },
        "nouvelleAdresse": { "ville": "Dumbéa", "codePostal": "98835" },
        "options": { "bp": "BP 123", "cedex": "CEDEX 9", "commentaire": "RAS" }
    }'::jsonb
);
```

### 4.2. Vérifications

1. **Backend :**

   * `GET /api/demat/or-demandes`
   * Vérifier dans Postman / navigateur que `resumeType`, `resumeAdresses`, `resumeDuree`, `resumeDates` sont présents.

2. **Frontend :**

   * Recharger la page liste admin.
   * Vérifier que la colonne “Demande OR” affiche les blocs lisibles.

---

## 🎓 Conclusion

Cette tâche a permis de :

* **Respecter les critères d’acceptation** :

  * plus de JSON brut
  * infos clés visibles immédiatement
* **Centraliser la logique métier dans le backend** :

  * calcul des durées
  * formatage des libellés
* **Simplifier le frontend** :

  * affichage uniquement
  * pas de parsing JSON côté Angular
* **Préparer le terrain pour d’autres usages** :

  * les mêmes résumés peuvent être réutilisés pour export CSV, PDF, etc.

Tu peux garder ce Markdown comme base de doc interne (Confluence, README de module, etc.).
