# 10️ **Mise à jour du front – Liste Admin OR**

## ✔ Création / modification du composant `ListeOrAdminComponent`

Ce composant permet d’afficher la liste des demandes OR et d’accéder au détail d’une ligne.

### TypeScript :

```ts
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

type AdminRow = { id: number; statut: string; createdDate: string; lastModifiedDate: string; };

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

### HTML :

```html
<tr *ngFor="let row of data">
  <td>{{ row.id }}</td>
  <td>{{ row.statut }}</td>
  <td>{{ row.createdDate | date: 'short' }}</td>
  <td>{{ row.lastModifiedDate | date: 'short' }}</td>
  <td><button class="btn btn-info btn-sm" (click)="voirDetail(row.id)">Voir détail</button></td>
</tr>
```

---

# 11️ **Ajout d’une page de détail côté front : `DemandeOrDetailComponent`**

Ce composant va :

✔ appeler le endpoint
✔ récupérer le DTO
✔ afficher l’info par blocs
✔ mettre le titre de l’onglet dynamiquement

---

### TypeScript :

```ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-demande-or-detail',
  templateUrl: './demande-or-detail.component.html',
})
export class DemandeOrDetailComponent implements OnInit {

  demandeOr: any = null;

  constructor(
      private route: ActivatedRoute,
      private http: HttpClient,
      private titleService: Title
    ) {}

    ngOnInit(): void {
      const id = Number(this.route.snapshot.paramMap.get('id'));
      this.loadDetail(id);
    }

    loadDetail(id: number): void {
      this.http.get(`/api/demat/or-demandes/${id}`).subscribe({
        next: data => {
          this.demandeOr = data;
          this.titleService.setTitle(`OR ${data.demandeurNom} - ${data.demandeurPrenom}`);
        },
        error: err => console.error('Erreur chargement détail OR', err),
      });
    }
}
```

---

### HTML :

```html
<div class="container mt-4" *ngIf="demandeOr">

  <div class="card mb-3">
    <div class="card-header">Demandeur</div>
    <div class="card-body">
      <div><strong>Nom :</strong> {{ demandeOr.demandeurNom }}</div>
      <div><strong>Prénom :</strong> {{ demandeOr.demandeurPrenom }}</div>
      <div><strong>Email :</strong> {{ demandeOr.demandeurEmail }}</div>
      <div><strong>Téléphone :</strong> {{ demandeOr.demandeurTelephone }}</div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-header">Ancienne adresse</div>
    <div class="card-body">
      <div>{{ demandeOr.ancienneAdresse.numEtVoie }}</div>
      <div>{{ demandeOr.ancienneAdresse.codePostal }} {{ demandeOr.ancienneAdresse.ville }}</div>
      <div>{{ demandeOr.ancienneAdresse.pays }}</div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-header">Nouvelle adresse</div>
    <div class="card-body">
      <div>{{ demandeOr.nouvelleAdresse.numEtVoie }}</div>
      <div>{{ demandeOr.nouvelleAdresse.codePostal }} {{ demandeOr.nouvelleAdresse.ville }}</div>
      <div>{{ demandeOr.nouvelleAdresse.pays }}</div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-header">Paramètres de réexpédition</div>
    <div class="card-body">
      <div><strong>Type :</strong> {{ demandeOr.typeReexpedition }}</div>
      <div><strong>Début :</strong> {{ demandeOr.dateDebut | date:'shortDate' }}</div>
      <div><strong>Fin :</strong> {{ demandeOr.dateFin | date:'shortDate' }}</div>
    </div>
  </div>

  <div class="card mb-3">
    <div class="card-header">Options</div>
    <div class="card-body">
      <div><strong>BP :</strong> {{ demandeOr.options?.bp }}</div>
      <div><strong>CEDEX :</strong> {{ demandeOr.options?.cedex }}</div>
      <div><strong>Commentaire :</strong> {{ demandeOr.options?.commentaire }}</div>
    </div>
  </div>

</div>
```

---

# 12️ **Mise à jour du `app-routing.module.ts`**

Ajout de la route vers la page détail :

```ts
{
  path: 'admin/or/:id',
  component: DemandeOrDetailComponent
}
```

---

# 13️ **Ajout i18n**

## JSON `demande-ordetail.json`

```json
{
  "siorApp": {
    "demandeOrdetails": {
      "title": "Demande OR détails",
      "tabTitle": "{{nom}} {{prenom}} - Demande OR"
    }
  }
}
```

---

### Utilisation dans le composant

```ts
this.titleService.setTitle(
  this.translateService.instant('siorApp.demandeOrdetails.tabTitle', {
    nom: data.demandeurNom,
    prenom: data.demandeurPrenom
  })
);
```

---

# 14️ **Résultat final**

✔ l’admin ne voit plus du JSON brut
✔ il voit une page lisible
✔ structurée par sections
✔ avec titre dynamique
✔ avec navigation OK
✔ tout est mappé correctement depuis la base
✔ conforme aux critères de la User Story OR-375


