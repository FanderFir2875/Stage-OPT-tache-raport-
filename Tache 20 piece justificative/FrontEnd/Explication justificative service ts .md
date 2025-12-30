# Frontend – Gestion des pièces justificatives

## Service Angular – `PieceJustificativeService`

### Fichier concerné

```
src/main/webapp/app/shared/piece-justificative/piece-justificative.service.ts
```

---

### Rôle du service

Le service `PieceJustificativeService` est responsable de la **communication HTTP entre le frontend Angular et l’API backend** pour l’upload des pièces justificatives.

Il permet :

* l’envoi d’un fichier (PDF / image) vers le backend,
* l’association de la pièce à une demande OR existante,
* le passage du type de pièce sélectionné par l’utilisateur.

Ce service est volontairement **simple et ciblé**, afin d’être facilement réutilisable dans différents composants si besoin.

---

###  Déclaration du service

```ts
@Injectable({ providedIn: 'root' })
export class PieceJustificativeService {
```

* `providedIn: 'root'` :

  * service singleton
  * disponible dans toute l’application
  * aucune déclaration supplémentaire dans un module requise

---

### Injection des dépendances

```ts
constructor(private http: HttpClient) {}
```

* Utilisation du `HttpClient` Angular
* Permet l’envoi de requêtes HTTP vers l’API REST

---

## Upload d’une pièce justificative

### Méthode exposée

```ts
upload(
  demandeOrId: number,
  file: File,
  type: string
): Observable<void>
```

---

### Paramètres

| Paramètre     | Type     | Description                           |
| ------------- | -------- | ------------------------------------- |
| `demandeOrId` | `number` | Identifiant de la demande OR          |
| `file`        | `File`   | Fichier sélectionné par l’utilisateur |
| `type`        | `string` | Type de pièce justificative           |

Le paramètre `type` correspond à l’énumération backend `TypePieceJustificative`.

---

### Construction du `FormData`

```ts
const formData = new FormData();
formData.append('file', file);
formData.append('type', type);
```

* Utilisation de `FormData` pour envoyer un fichier
* Compatible avec le `multipart/form-data` attendu par le backend
* Les clés (`file`, `type`) correspondent exactement aux `@RequestParam` du controller Spring

---

### Appel HTTP

```ts
return this.http.post<void>(
  `/api/or-demandes/${demandeOrId}/pieces`,
  formData
);
```

* Appel POST vers l’endpoint REST backend
* URL paramétrée avec l’ID de la demande OR
* Aucun body JSON : uniquement le `FormData`
* La réponse ne contient pas de payload (`void`)

---

### Intégration dans le composant

Ce service est utilisé dans :

* `FormulaireOrComponent`
* déclenché lors de la finalisation de la demande
* permet l’upload automatique vers MinIO après création de la demande OR

---

### Résumé

* Service Angular simple et dédié
* Gestion propre du `multipart/form-data`
* Alignement strict avec l’API backend
* Facilement extensible (suppression, liste des pièces, etc.)

---

**PieceJustificativeService.ts :**
``` java
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PieceJustificativeService {
constructor(private http: HttpClient) {}

  upload(demandeOrId: number, file: File, type: string): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return this.http.post<void>(
      `/api/or-demandes/${demandeOrId}/pieces`,
      formData
    );
  }
}

```