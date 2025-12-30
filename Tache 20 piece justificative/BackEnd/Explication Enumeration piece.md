# Enum métier – `TypePieceJustificative`

### Fichier concerné

```
src/main/java/nc/opt/sior/domain/enumeration/TypePieceJustificative.java
```

---

### Objectif de l’enum

L’enum `TypePieceJustificative` permet de **définir et restreindre les types de pièces justificatives** acceptées lors de la création d’un Ordre de Réexpédition.

Il joue un rôle central dans :

* la **validation métier** côté backend,
* la **cohérence des données** stockées en base,
* l’alignement entre le **frontend** et le **backend**.

---

### Contenu de l’enum

```java
public enum TypePieceJustificative {
    CARTE_IDENTITE,
    PASSEPORT,
    JUSTIFICATIF_DOMICILE,
    AUTRE
}
```

---

### Description des valeurs

| Valeur                  | Description                                           |
| ----------------------- | ----------------------------------------------------- |
| `CARTE_IDENTITE`        | Carte nationale d’identité                            |
| `PASSEPORT`             | Passeport                                             |
| `JUSTIFICATIF_DOMICILE` | Justificatif de domicile (facture, attestation, etc.) |
| `AUTRE`                 | Toute autre pièce justificative non standard          |

---

### Lien avec les autres couches

* **Base de données**
  Le champ `type` de la table `piece_justificative` stocke la valeur de cet enum sous forme de `VARCHAR`.

* **Entity JPA**
  L’enum est utilisé avec l’annotation :

  ```java
  @Enumerated(EnumType.STRING)
  ```

  afin de stocker la valeur textuelle et non l’ordinal.

* **API REST**
  Le type de pièce est transmis depuis le frontend (Angular) lors de l’upload de la pièce justificative.

* **Frontend**
  Les valeurs proposées dans la liste déroulante correspondent exactement à celles définies dans cet enum.

---

### Avantages de cette approche

* ✔ Évite les valeurs invalides ou incohérentes
* ✔ Facilite l’évolution (ajout d’un nouveau type)
* ✔ Garantit l’alignement front / back
* ✔ Améliore la lisibilité du code métier

