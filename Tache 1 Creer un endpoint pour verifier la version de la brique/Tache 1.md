
# 📦 Endpoint technique — `/or-demat/version`

## 📝 Description

Avant de commencer les développements métier liés à la **dématérialisation des OR**, il est nécessaire d’exposer un **endpoint simple** permettant d’afficher la **version du module `or-demat`**.

Cet endpoint sert de **point de contrôle technique**, pour vérifier :

- si le module est **présent**
- si le module est **accessible**
- si le module est **correctement déployé**

---

## ⚙️ Détails techniques

| Élément               | Valeur                                                   |
|-----------------------|----------------------------------------------------------|
| **URL**               | `GET /api/or-demat/version`                              |
| **Contrôleur**        | `OrDematController`                                      |
| **Package**           | `nc.opt.sior.web.rest`                                   |
| **Format de réponse** | `application/json`                                       |
| **Version**           | retournée en dur pour cette première étape (`"1.0.0"`)   |

### ✅ Exemple de réponse JSON

```json
{
  "module": "or-demat",
  "version": "1.0.0"
}
````

---

## 🧠 Implémentation — Contrôleur

Ce contrôleur expose l’endpoint HTTP accessible publiquement, et retourne la version actuelle du module.

```java
package nc.opt.sior.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RequestMapping("/api") // Préfixe URL commun
@RestController
public class OrDematController {

    @GetMapping("/or-demat/version")
    public ResponseEntity<Map<String , String>> getVersion() {
        Map<String, String> response = Map.of(
            "module", "or-demat",
            "version", "1.0.0"
        );
        return ResponseEntity.ok(response);
    }
}
```

---

## 🔓 Configuration de sécurité

Ajout dans le `SecurityConfiguration` pour autoriser l’accès **SANS AUTHENTIFICATION** à cet endpoint uniquement.

```java
.antMatchers("/api/or-demat/version").permitAll()
```

*(Ajouté avant le `antMatchers("/api/**").authenticated()` pour ne pas être bloqué)*

---

## ✅ Test unitaire

Ce test vérifie que l’endpoint retourne bien le JSON attendu avec un `HTTP 200 OK`.

```java
package nc.opt.sior.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OrDematControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new OrDematController())
        .build();

    @Test
    void shouldReturnVersionSuccessfully() throws Exception {
        mockMvc.perform(get("/api/or-demat/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.module").value("or-demat"))
            .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
```

---

## ✅ Critères d’acceptation

| Critère                                                       | Statut |
| ------------------------------------------------------------- | ------ |
| Endpoint accessible sans authentification                     | ✅      |
| Retourne un code HTTP `200 OK`                                | ✅      |
| Retourne un JSON contenant `module` et `version`              | ✅      |
| Les valeurs correspondent bien à `"or-demat"` et `"1.0.0"`    | ✅      |
| Test unitaire validant le comportement présent et fonctionnel | ✅      |



