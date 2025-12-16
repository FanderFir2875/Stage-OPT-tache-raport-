# Implémentation FRONT – Sélection Offres & Forfaits

## Formulaire OR dématérialisé (OR définitif)

---

## Objectif côté Front

Permettre à l’usager de :

* Sélectionner une **Offre OR définitif**
* Sélectionner un **Forfait associé**
* Voir la **durée et le prix**
* Ne **plus saisir la date de fin**
* Envoyer uniquement :

  * `dateDebut`
  * `forfaitId`

---

## Règles fonctionnelles (alignées avec le backend)

* Une seule offre affichée : **OR Définitif**
* Forfaits disponibles :

  * 6 mois
  * 12 mois
* La **date de fin est calculée côté backend**
* Le formulaire **n’envoie pas `dateFin`**

---

## Architecture Front retenue

### Composant

* `FormulaireOrComponent`

### Services utilisés

* `OrService` → création de la demande
* `OffreService` → chargement des offres
* `ForfaitService` → chargement des forfaits liés à l’offre

---

## Modèle de données côté Front

### Interface minimale (exemple)

```ts
export interface IOffre {
  id: number;
  code: string;
  libelle: string;
}

export interface IForfait {
  id: number;
  libelle: string;
  duree: number;
  prix: number;
  espace: string;
}
```

---

## FormGroup – évolution du formulaire

### Avant

```ts
dateFin: ['', Validators.required]
```

### Maintenant

```ts
this.formulaireOr = this.fb.group({
  dateDebut: ['', Validators.required],
  forfaitId: [null, Validators.required],

  demandeurNom: ['', Validators.required],
  demandeurPrenom: ['', Validators.required],
  demandeurEmail: ['', [Validators.required, Validators.email]],
  demandeurTelephone: [''],

  rechercheAncienne: [''],
  rechercheNouvelle: [''],

  ancienneAdresse: this.fb.group({
    numEtVoie: ['', Validators.required],
    ville: ['', Validators.required],
    codePostal: ['', Validators.required],
  }),

  nouvelleAdresse: this.fb.group({
    numEtVoie: ['', Validators.required],
    ville: ['', Validators.required],
    codePostal: ['', Validators.required],
  }),
});
```

---

## Chargement des offres & forfaits

### Dans le composant TS

```ts
offres: IOffre[] = [];
forfaits: IForfait[] = [];

selectedOffre?: IOffre;
```

### Au `ngOnInit`

```ts
ngOnInit(): void {
  this.loadOffres();
}
```

### Chargement des offres

```ts
loadOffres(): void {
  this.offreService.getAll().subscribe(offres => {
    // OR-D uniquement
    this.offres = offres.filter(o => o.code === 'OR-D');
    this.selectedOffre = this.offres[0];
    this.loadForfaits(this.selectedOffre.id);
  });
}
```

### Chargement des forfaits liés à l’offre

```ts
loadForfaits(offreId: number): void {
  this.forfaitService.getByOffre(offreId).subscribe(forfaits => {
    this.forfaits = forfaits.filter(f => f.duree === 6 || f.duree === 12);
  });
}
```

---

## HTML – Sélection Offre

```html
<div class="card mb-3">
  <div class="card-header">
    <strong>Offre</strong>
  </div>

  <div class="card-body">
    <div class="btn-group">
      <button
        class="btn btn-primary"
        disabled
      >
        {{ selectedOffre?.libelle || 'Non renseigné' }}
      </button>
    </div>
  </div>
</div>
```

Offre fixe → non cliquable

---

## HTML – Sélection Forfait

```html
<div class="card mb-3">
  <div class="card-header">
    <strong>Forfait</strong>
  </div>

  <div class="card-body">
    <div class="list-group">
      <label
        class="list-group-item"
        *ngFor="let forfait of forfaits"
      >
        <input
          type="radio"
          formControlName="forfaitId"
          [value]="forfait.id"
          class="form-check-input me-2"
        />
        <strong>{{ forfait.libelle }}</strong>
        <span class="text-muted ms-2">
          ({{ forfait.duree }} mois – {{ forfait.prix }} F)
        </span>
      </label>
    </div>
  </div>
</div>
```

---

## Date de fin – information utilisateur (optionnelle)

> **Affichage informatif uniquement**

```html
<div *ngIf="formulaireOr.get('dateDebut')?.value && formulaireOr.get('forfaitId')?.value">
  <small class="text-muted">
    La date de fin sera calculée automatiquement selon le forfait choisi.
  </small>
</div>
```

---

## Payload envoyé au backend

```ts
onSubmit(): void {
  const formValue = this.formulaireOr.value;

  const payload = {
    ...formValue,
    dateDebut: moment(formValue.dateDebut).format('YYYY-MM-DD')
  };

  this.orService.createOrDemat(payload).subscribe(...);
}
```

### Payload réel envoyé

```json
{
  "dateDebut": "2025-12-09",
  "forfaitId": 8,
  "demandeurNom": "DUPONT",
  "demandeurPrenom": "Alexandre",
  "demandeurEmail": "alex@test.nc",
  "ancienneAdresse": { ... },
  "nouvelleAdresse": { ... }
}
```

---

## Résultat côté Front

- Plus de `dateFin` dans le formulaire
- Sélection claire Offre / Forfait
- Durée & prix visibles
- Validation alignée backend
- UX cohérente avec le métier

---

## Prochaines étapes (pour le nouveau chat)

1. Page **Admin – Liste des contrats**
2. Page **Admin – Détail contrat**
3. Gestion des champs **“Non renseigné”**
4. Ajustements UI / SCSS


