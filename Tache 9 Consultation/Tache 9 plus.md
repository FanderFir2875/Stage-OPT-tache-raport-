#  Ajout d’une nouvelle colonne : `createdDate` dans la Liste OR Admin**

## Objectif

Afficher la **date de création** de chaque Demande OR dans la page d'administration :

* Mise à jour du **DTO backend**
* Adaptation du **service backend**
* Modification du **template Angular**
* Extension du **modèle TypeScript**
* Ajout de la **clé i18n**
* Mise à jour de la **table HTML**

---

# 1. Backend — Mise à jour du DTO

Fichier :
`src/main/java/nc/opt/sior/service/dto/DemandeOrAdminListDTO.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeOrAdminListDTO {

    private Long id;
    private String statut;
    private Instant createdDate;
    private Instant lastModifiedDate;
}
```

### Corrections effectuées

* Ajout du champ `createdDate`
* Correction du nom (donné en majuscule par erreur, `CreateDate`)
* Utilisation du type `Instant` (cohérent avec JHipster)

---

# 2. Backend — Adaptation du service

Méthode `listAllForAdmin()` dans `DemandeOrService` :


### Update de la classe méthode `listAllForAdmin()`

```java
public List<DemandeOrAdminListDTO> listAllForAdmin() {
    return demandeOrService.findAll().stream()
        .map(or -> new DemandeOrAdminListDTO(
            or.getId(),
            JsonUtils.getStringOrDefault(or.getData(), "statut", "INCONNU"),
            or.getCreatedDate(),
            or.getLastModifiedDate()
        ))
        .collect(Collectors.toList());
}
```

### Explication

* `or.getCreatedDate()` récupère automatiquement la colonne `created_date` en BDD (auditing JHipster)
* Le DTO renvoie maintenant 4 champs correctement ordonnés

---

# 3. Frontend — Extension du modèle TypeScript

Dans :
`liste-or-admin.component.ts`

### Ancien modèle

```ts
type AdminRow = { id: number; statut: string; lastModifiedDate: string };
```

###  Nouveau modèle

```ts
type AdminRow = {
  id: number;
  statut: string;
  createdDate: string;
  lastModifiedDate: string;
};
```

---

# 4. Frontend — Mise à jour du template HTML

Fichier :
`liste-or-admin.component.html`

### Ajout de la colonne `Date de création`

```html
<th jhiTranslate="sior.listeOrAdmin.columns.createdDate">
  Date de création
</th>
```

Et dans le `<tbody>` :

```html
<td>{{ row.createdDate | date: 'short' }}</td>
```

### Version complète du tableau :

```html
<table class="table table-striped" *ngIf="data.length > 0">
  <thead>
    <tr>
      <th jhiTranslate="sior.listeOrAdmin.columns.id"></th>
      <th jhiTranslate="sior.listeOrAdmin.columns.statut"></th>
      <th jhiTranslate="sior.listeOrAdmin.columns.createdDate"></th>
      <th jhiTranslate="sior.listeOrAdmin.columns.lastModifiedDate"></th>
    </tr>
  </thead>
  <tbody>
    <tr *ngFor="let row of data">
      <td>{{ row.id }}</td>
      <td>{{ row.statut }}</td>
      <td>{{ row.createdDate | date:'short' }}</td>
      <td>{{ row.lastModifiedDate | date:'short' }}</td>
    </tr>
  </tbody>
</table>
```

---

# 5. i18n — Ajout des nouvelles clés

Fichier :
`src/main/webapp/i18n/fr/liste-or-admin.json`

### Ajout :

```json
"createdDate": "Date de création"
```

### Version complète :

```json
{
  "sior": {
    "listeOrAdmin": {
      "title": "Demandes OR (Admin)",
      "columns": {
        "id": "Numéro de demande",
        "statut": "Statut",
        "createdDate": "Date de création",
        "lastModifiedDate": "Dernière mise à jour"
      },
      "noData": "Aucune demande trouvée."
    }
  }
}
```

---

# 6. Résultat final dans la page

La page affiche désormais :

| ID  | Statut     | Date de création | Dernière mise à jour |
| --- | ---------- | ---------------- | -------------------- |
| 1   | EN_ATTENTE | 13/11/2025 19:58 | 13/11/2025 19:58     |
| ... | ...        | ...              | ...                  |

Avec formatage automatique grâce au pipe Angular :

```html
{{ row.createdDate | date:'short' }}
```

---

# Conclusion

L’ajout de la colonne **createdDate** a nécessité la modification :

| Couche   | Fichier             | État                |
| -------- | ------------------- | ------------------- |
| Backend  | DTO                 | ✔️ Mis à jour       |
| Backend  | Service liste admin | ✔️ Adapté           |
| Frontend | modèle TS           | ✔️ Ajout du champ   |
| Frontend | HTML                | ✔️ Nouvelle colonne |
| Frontend | i18n                | ✔️ Nouvelle clé     |

Tout est maintenant parfaitement synchronisé avec la BDD et l’API, et l’affichage correspond exactement aux données de Postman.

