# Tâche OR-372 — Lister toutes les demandes OR (Admin DSI)

## Objectif

Permettre à un utilisateur disposant du rôle `ADMIN_DSI` de consulter **toutes les demandes d’Ordres de Réexpédition (OR)** dans le système, sans restriction par utilisateur.
L’objectif est de fournir une **vue globale** pour la supervision et le suivi des réexpéditions.

---

## Compréhension du besoin

Cette tâche vise à créer :

* une **route sécurisée côté backend** pour exposer toutes les demandes OR,
* une **page Angular** affichant ces données,
* et un **lien d’accès réservé aux DSI** dans le menu d’administration.

---

## Architecture globale

| Couche   | Élément ajouté                      | Rôle                                                   |
| -------- | ----------------------------------- | ------------------------------------------------------ |
| Backend  | `OrAdminResource` (REST Controller) | Expose la liste complète des demandes OR               |
| Backend  | `DemandeOrService.findAll()`        | Méthode métier pour récupérer les données              |
| Frontend | `ListeOrAdminComponent`             | Affiche la liste des demandes dans l’IHM               |
| Frontend | Route `/admin/liste-or`             | Permet d’accéder à la page                             |
| Frontend | Menu “Demandes OR (Admin)”          | Lien de navigation visible uniquement pour `ADMIN_DSI` |

---

## BACKEND — Spring Boot

### Service métier

Fichier : `src/main/java/nc/opt/sior/service/DemandeOrService.java`

```java
package nc.opt.sior.service;

import lombok.extern.slf4j.Slf4j;
import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.repository.DemandeOrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class DemandeOrService {

    private final DemandeOrRepository demandeOrRepository;

    public DemandeOrService(DemandeOrRepository demandeOrRepository) {
        this.demandeOrRepository = demandeOrRepository;
    }

    /**
     * Récupère toutes les Demandes OR.
     */
    @Transactional(readOnly = true)
    public List<DemandeOr> findAll() {
        log.debug("📦 Récupération de toutes les demandes OR (ADMIN_DSI)");
        return demandeOrRepository.findAll();
    }
}
```

**Explications :**

* `@Transactional(readOnly = true)` : optimisation pour les lectures.
* `findAll()` appelle directement le `JpaRepository` pour lister toutes les entités `DemandeOr`.
* Pas de filtrage par utilisateur : le DSI voit toutes les demandes.

---

### Contrôleur REST sécurisé

Fichier : `src/main/java/nc/opt/sior/web/rest/OrAdminResource.java`

```java
package nc.opt.sior.web.rest;

import lombok.extern.slf4j.Slf4j;
import nc.opt.sior.domain.DemandeOr;
import nc.opt.sior.service.DemandeOrService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class OrAdminResource {

    private final DemandeOrService demandeOrService;

    public OrAdminResource(DemandeOrService demandeOrService) {
        this.demandeOrService = demandeOrService;
    }

    /**
     * GET /or-demandes : Récupère toutes les demandes OR (ADMIN_DSI uniquement).
     */
    @GetMapping("/or-demandes")
    @PreAuthorize("hasAuthority('ADMIN_DSI')")
    public ResponseEntity<List<DemandeOr>> getAllDemandesOr() {
        log.info("📋 Consultation de toutes les demandes OR par un ADMIN_DSI");
        List<DemandeOr> demandes = demandeOrService.findAll();
        return ResponseEntity.ok(demandes);
    }
}
```

**Sécurité :**

* `@PreAuthorize("hasAuthority('ADMIN_DSI')")` :
  empêche tout autre rôle d’appeler l’endpoint.
* Accessible uniquement via `/api/admin/or-demandes`.

---

### Exemple de réponse JSON

```json
[
  {
    "id": 101,
    "statut": "EN_ATTENTE",
    "dateDebut": "2025-10-01T00:00:00Z",
    "dateFin": "2025-10-31T00:00:00Z",
    "lastModifiedDate": "2025-11-02T15:45:21Z"
  },
  {
    "id": 102,
    "statut": "VALIDÉ",
    "dateDebut": "2025-09-01T00:00:00Z",
    "dateFin": "2025-10-01T00:00:00Z",
    "lastModifiedDate": "2025-11-03T10:15:00Z"
  }
]
```

---

## FRONTEND — Angular (JHipster)

### Création du composant

Commande utilisée :

```bash
ng generate component liste-or-admin
```

Structure créée :

```
src/main/webapp/app/liste-or-admin/
 ├── liste-or-admin.component.ts
 ├── liste-or-admin.component.html
 ├── liste-or-admin.component.scss
 └── liste-or-admin.component.spec.ts
```

---

### Logique TypeScript

Fichier : `liste-or-admin.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

type AdminRow = { id: number; statut: string; lastModifiedDate: string; };

@Component({
  selector: 'app-liste-or-admin',
  templateUrl: './liste-or-admin.component.html',
})
export class ListeOrAdminComponent implements OnInit {
  data: AdminRow[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<AdminRow[]>('/api/admin/or-demandes').subscribe({
      next: rows => (this.data = rows),
      error: err => console.error('Erreur chargement demandes OR admin', err),
    });
  }
}
```
---

# Explication détaillée du component TypeScript

## Explication complète (pédagogique)

### 1. Le rôle du composant

Ce composant :

* contacte le backend
* récupère la liste des demandes OR
* les stocke dans `data`
* et laisse Angular afficher le tableau dans le HTML

### 2. Le décorateur `@Component`

```ts
@Component({
  selector: 'app-liste-or-admin',
  templateUrl: './liste-or-admin.component.html',
})
```

* `selector`: nom de la balise HTML du composant (utile seulement si on l’insère manuellement)
* `templateUrl`: fichier HTML associé

Comme on accède via une route, **le selector n’est pas utilisé directement**.

---

### 3. Injection du HttpClient

```ts
constructor(private http: HttpClient) {}
```

Permet d’appeler les API REST du backend.

JHipster configure automatiquement HttpClient dans l'application.

---

### 4. Appel de l’API dans `ngOnInit()`

`ngOnInit()` = méthode appelée quand Angular charge la page.

```ts
ngOnInit(): void {
  this.http.get<AdminRow[]>('/api/admin/or-demandes')
```

Ce `GET` correspond exactement à l’endpoint :

```
GET /api/admin/or-demandes
```

---

### 5. Le subscribe()

```ts
.subscribe({
  next: rows => (this.data = rows),
  error: err => console.error(...)
});
```

Angular utilise un système d’**Observable** → il faut s’y abonner.

✔ `next:` → la réponse arrive → on met les données dans `this.data`
✔ `error:` → gestion des erreurs

---

### 6. Le rôle de `data: AdminRow[] = []`

* `data` est un tableau vide au départ
* Quand le backend répond → rempli avec les résultats
* Le template utilise `*ngFor="let row of data"`

Angular met automatiquement à jour le DOM (data-binding)

---




---

### Template HTML

Fichier : `liste-or-admin.component.html`

```html
<div class="container mt-4">
  <h1 class="text-center">Liste des Demandes OR (Admin)</h1>
  <p class="text-muted text-center">Vue de supervision des ordres de réexpédition</p>

  <table class="table table-striped mt-4" *ngIf="data.length > 0; else empty">
    <thead>
      <tr>
        <th>ID</th>
        <th>Statut</th>
        <th>Dernière mise à jour</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let row of data">
        <td>{{ row.id }}</td>
        <td>{{ row.statut }}</td>
        <td>{{ row.lastModifiedDate | date:'short' }}</td>
      </tr>
    </tbody>
  </table>

  <ng-template #empty>
    <div class="alert alert-info text-center mt-4">Aucune demande OR trouvée.</div>
  </ng-template>
</div>
```

**Explications :**

* `*ngFor` : itère sur la liste des OR.
* `*ngIf` : affiche le tableau seulement s’il y a des données.
* `ng-template #empty` : message d’absence de données.
* `| date:'short'` : formate la date proprement.

---

### Routing Angular

Dans `sior-app-routing.module.ts` :

```typescript
{
  path: 'admin/liste-or',
  component: ListeOrAdminComponent,
  canActivate: [UserRouteAccessService],
  data: { authorities: [Authority.ADMIN_DSI] },
}
```

**Sécurité front :**
Même logique que le backend → le `UserRouteAccessService` empêche l’accès si l’utilisateur n’est pas un `ADMIN_DSI`.

---

### Lien dans la barre de navigation

Fichier : `navbar.component.html`

```html
<li>
  <a (click)="collapseNavbar()" class="dropdown-item" routerLink="/admin/liste-or" routerLinkActive="active">
    <fa-icon [fixedWidth]="true" icon="clipboard-list"></fa-icon>
    <span>Demandes OR (Admin)</span>
  </a>
</li>
```

Visible uniquement pour les compte ayant les droits `ADMIN_DSI`.

---
## Concepts Angular abordés

| Concept                  | Description                                           |
| ------------------------ | ----------------------------------------------------- |
| `Component`              | Composant Angular = unité visuelle + logique associée |
| `OnInit`                 | Méthode exécutée à l’initialisation du composant      |
| `HttpClient`             | Permet d’appeler les APIs REST du backend             |
| `subscribe()`            | Gère les Observables asynchrones                      |
| `routerLink`             | Navigation interne sans rechargement                  |
| `UserRouteAccessService` | Vérifie les rôles utilisateurs côté front             |
| `Authority.ADMIN_DSI`    | Constante utilisée pour le contrôle des accès         |

---
## Étape complémentaire — Gestion du i18n et corrections front-end

### Contexte

Après la mise en place du composant **Liste des Demandes OR (Admin)** et du lien dans le menu d’administration,
nous avons ajouté la **gestion du i18n (internationalisation)** pour rendre la page multilingue
et résolu plusieurs erreurs liées à la traduction et au routage Angular.

---

## Internationalisation (i18n)

L’objectif est d’afficher tous les libellés et titres de la page via le système de traduction JHipster
afin de pouvoir facilement gérer plusieurs langues (par défaut : **français** et **anglais**).

### Structure des fichiers i18n

Les traductions sont stockées dans les fichiers JSON suivants :

```
src/main/webapp/i18n/
 ├── fr/
      └── liste-or-admin.json

```

### Contenu du fichier français

`src/main/webapp/i18n/fr/liste-or-admin.json`

```json
{
  "sior": {
    "listeOrAdmin": {
      "title": "Demandes OR (Admin)",
      "subtitle": "Vue de supervision des ordres de réexpédition",
      "columns": {
        "id": "Numéro de demande",
        "statut": "Statut",
        "lastModifiedDate": "Dernière mise à jour"
      },
      "noData": "Aucune demande trouvée."
    }
  }
}
```

---

## Intégration dans le template Angular

Dans `liste-or-admin.component.html`, nous avons remplacé le texte statique par les clés i18n :

```html
<div class="container mt-4">
  <h1 class="text-center" jhiTranslate="sior.listeOrAdmin.title">Demandes OR (Admin)</h1>
  <p class="text-muted text-center" jhiTranslate="sior.listeOrAdmin.subtitle">
    Vue de supervision des ordres de réexpédition
  </p>

  <table class="table table-striped mt-4" *ngIf="data.length > 0; else empty">
    <thead>
      <tr>
        <th jhiTranslate="sior.listeOrAdmin.columns.id">Numéro de demande</th>
        <th jhiTranslate="sior.listeOrAdmin.columns.statut">Statut</th>
        <th jhiTranslate="sior.listeOrAdmin.columns.lastModifiedDate">Dernière mise à jour</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let row of data">
        <td>{{ row.id }}</td>
        <td>{{ row.statut }}</td>
        <td>{{ row.lastModifiedDate | date:'short' }}</td>
      </tr>
    </tbody>
  </table>

  <ng-template #empty>
    <div class="alert alert-info text-center mt-4" jhiTranslate="sior.listeOrAdmin.noData">
      Aucune demande OR trouvée.
    </div>
  </ng-template>
</div>
```

---

## Résolution du message `translation-not-found[...]`

Lors des premiers tests, la page affichait :

```
translation-not-found[Demandes OR (Admin)]
```

Cela provenait d’une mauvaise utilisation de `jhiTranslate`.

### Exemple erroné :

```html
<h1 jhiTranslate="Demandes OR (Admin)">Demandes OR (Admin)</h1>
```

Ici, Angular cherche une clé `"Demandes OR (Admin)"` dans les fichiers JSON (clé inexistante).

### Correction :

```html
<h1 jhiTranslate="sior.listeOrAdmin.title">Demandes OR (Admin)</h1>
```

On utilise la **clé** définie dans le fichier i18n (`sior.listeOrAdmin.title`).

Le texte entre balises `<h1>...</h1>` reste un **fallback**,
utilisé uniquement si la traduction ne se charge pas.

---

## Vérification et bonnes pratiques

### Vérifier le chargement du fichier i18n

1. Ouvrir la console navigateur (`F12 → Network`).
2. Recharger la page.
3. Vérifier la requête :

   ```
   GET http://localhost:9000/i18n/fr/liste-or-admin.json
   ```

   Doit retourner un code **200 OK**.

### Si la traduction ne s’applique pas :

* Vérifier le nom du fichier (`liste-or-admin.json` en minuscules).
* Relancer le front (`npm start`).
* Vider le cache du navigateur (`Ctrl + F5`).

---

## Concepts Angular/JHipster appliqués

| Concept                    | Description                                                                            |
| -------------------------- | -------------------------------------------------------------------------------------- |
| **`jhiTranslate`**         | Directive fournie par JHipster (basée sur `ngx-translate`) pour gérer les traductions. |
| **Fallback text**          | Texte affiché si la clé i18n est introuvable.                                          |
| **Structure hiérarchique** | Les clés JSON suivent la logique du module (`sior.listeOrAdmin.*`).                    |

---

## Résultat final après ajout du i18n

* Page `/admin/liste-or` toujours accessible uniquement aux utilisateurs `ADMIN_DSI`.
* Textes entièrement traduits via le système i18n.
* Disparition du message `translation-not-found[...]`.
* Fichiers i18n organisés et prêts pour l’ajout d’autres langues.

---

## Points appris et bonnes pratiques

1. Toujours utiliser une **clé de traduction** (et non le texte brut) avec `jhiTranslate`.
2. Nommer les fichiers i18n selon le composant (`liste-or-admin.json`).
3. Vérifier le **chargement réseau** pour détecter les 404 de traduction.
4. Redémarrer le front (`npm start`) après ajout d’un nouveau fichier i18n.
5. Garder une **structure claire et modulaire** (`sior.nomDuComposant`).

---

## Résultat final

* Une page `/admin/liste-or` visible uniquement par les administrateurs DSI.
* Affichage clair et filtré de toutes les demandes OR.
* Intégration complète front–back.
* Fonctionnalité testée et compilée avec succès via  `./gradlew bootRun`. (Il n'y a plus besoin de passer par npm start.)

---

## Structure finale du projet

```
src/
 ├── main/java/nc/opt/sior/
 │    ├── domain/DemandeOr.java
 │    ├── repository/DemandeOrRepository.java
 │    ├── service/DemandeOrService.java
 │    └── web/rest/OrAdminResource.java
 │
 └── main/webapp/app/
      ├── liste-or-admin/
      │    ├── liste-or-admin.component.ts
      │    ├── liste-or-admin.component.html
      │    └── liste-or-admin.component.scss
      ├── layouts/navbar/navbar.component.html
      └── sior-app-routing.module.ts
```







