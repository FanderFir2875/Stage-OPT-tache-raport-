# 1. Reactive Forms : c’est quoi exactement ?

Un **Reactive Form** est un formulaire :

* **piloté en TypeScript**, pas dans le HTML
* dont les champs sont des **objets JS** (FormControl, FormGroup)
* qui permet de faire de la **validation**, de la **réactivité**, et du **test unitaire** facilement
* qui suit une logique **reactive programming** (Observable, state management)

Contrairement au **Template-driven form**, ici **le TS contrôle tout**, pas le HTML.

---

# 2. Les éléments de base : FormControl, FormGroup, FormBuilder

## 2.1 FormControl

Un `FormControl` représente **un champ unique** de formulaire (input, select, textarea…).

Exemple :

```ts
const nom = new FormControl('Jean');
```

Tu peux lui appliquer :

* une **valeur** (“Jean”)
* des **validators** (`Validators.required`, etc.)
* des **listeners** pour détecter les changements

```ts
const nom = new FormControl('', Validators.required);
```

---

## 2.2 FormGroup

Un `FormGroup` est un **ensemble de FormControls** (un formulaire complet).

Exemple :

```ts
const form = new FormGroup({
  nom: new FormControl(''),
  prenom: new FormControl(''),
});
```

C’est littéralement un **objet JavaScript** :

```json
{
  nom: "Jean",
  prenom: "Pierre"
}
```

---

## 2.3 FormBuilder

C'est juste un **outil pratique** pour construire plus facilement le FormGroup.

Au lieu d’écrire :

```ts
new FormGroup({
  nom: new FormControl(''),
  prenom: new FormControl(''),
});
```

On écrit :

```ts
this.fb.group({
  nom: [''],
  prenom: [''],
});
```

Ça rend le code plus court, plus lisible.

---

# 3. Comment Angular lie TS ↔ HTML ?

Très simple :

* Dans le HTML → `formGroup` et `formControlName`
* Dans le TS → tu gères le FormGroup / FormControls

Exemple :

### **TypeScript**

```ts
form = this.fb.group({
  nom: ['', Validators.required],
});
```

### **HTML**

```html
<form [formGroup]="form">
  <input formControlName="nom">
</form>
```

Quand l’utilisateur tape dans l’input, Angular met AUTOMATIQUEMENT à jour `form.controls.nom.value`.

Pas besoin d’event listener.

---

# 4. Gestion de la validation

### Chaque FormControl peut avoir des validators :

```ts
this.fb.group({
  nom: ['', [Validators.required, Validators.minLength(3)]],
});
```

### Lire l’état du champ

Angular donne plein d’infos :

* `form.get('nom')?.invalid`
* `form.get('nom')?.valid`
* `form.get('nom')?.touched` (déjà cliqué)
* `form.get('nom')?.dirty` (modifié)
* `form.get('nom')?.errors` (liste des erreurs)

Grâce à ça, on peut afficher de beaux messages d’erreur dans le HTML :

```html
<div *ngIf="form.get('nom')?.touched && form.get('nom')?.invalid">
  Le nom est obligatoire
</div>
```

---

# 5. Récupération des données du formulaire

Les données du formulaire sont dans :

```ts
this.form.value
```

C’est un **objet JS** qui ressemble à :

```json
{
  "nom": "Paul",
  "prenom": "Martin"
}
```

Tu peux ensuite envoyer ça au backend :

```ts
this.demandeOrService.create(this.form.value).subscribe(...);
```

---

# 6. Réactivité : un vrai avantage

ON peux écouter les changements d’un champ :

```ts
this.form.get('nom')?.valueChanges.subscribe(value => {
  console.log("Le nom a changé :", value);
});
```

Ainsi qu'à réagir dynamiquement :

* activer/désactiver un champ
* mettre une valeur automatique
* afficher/cacher une section
* valider un champ selon un autre champ

Ex :

```ts
this.form.get('dateDebut')?.valueChanges.subscribe(date => {
  if (date > this.form.get('dateFin')?.value) {
    this.form.get('dateFin')?.setErrors({ dateInvalide: true });
  }
});
```

Ça, c’est le genre de truc très difficile à faire en Template-driven forms.

---

# 7. Reactive Forms = parfait pour les tests

ON peut tester automatiquement :

```ts
form.controls.nom.setValue('');
expect(form.valid).toBeFalse();
```
---



# 8. Résumé simple et clair

| Concept             | Définition                     | Exemple                         |
| ------------------- | ------------------------------ | ------------------------------- |
| **FormControl**     | un champ                       | `new FormControl('')`           |
| **FormGroup**       | groupe de champs               | `new FormGroup({ nom: ... })`   |
| **FormBuilder**     | raccourci pour créer les forms | `this.fb.group({...})`          |
| **formControlName** | relie l’input au FormControl   | `<input formControlName="nom">` |
| **Validators**      | règles de validation           | `Validators.required`           |
| **valueChanges**    | écoute les changements         | `.valueChanges.subscribe()`     |
| **form.value**      | récupère les données           | `{ nom: "Jean" }`               |

---

