# springboot-web-error

## Présentation
Le projet *springboot-web-error* est la librairie pour la gestion des erreurs d'une API Spring Boot.

## Frameworks
- [Spring boot](https://spring.io/projects/spring-boot) [@2.7.15](https://docs.spring.io/spring-boot/docs/2.7.15/reference/html)

## Dependencies
- [FLC Commons Core](https://github.com/flc-it/springboot-commons-core)

## Packages
**org.flcit.springboot.web.error.domain** => classes de l'objet erreur envoyé à l'appelant
  
**org.flcit.springboot.web.error.ErrorControllerAdvice** => classe qui gère les exceptions avec logging + envoi de l'erreur

## Fonctionnement
Ne pas traiter les exceptions dans le code applicatif => elles sont catchées, traitées et loggées globalement dans la classe **ErrorControllerAdvice** de cette librairie.
    
Les exceptions fonctionnelles doivent être catchées dans le code et renvoyées via une Exception du package **org.flcit.springboot.commons.core.exception** adaptée au cas.
Si il n'y a pas d'exception adaptée, il est possible de créer une classe Exception en étendant l'exception de base **org.flcit.commons.core.exception.BasicRuntimeException**.

## Erreur
Format de l'erreur renvoyé à l'appelant :
```javascript
{
    "path": "/flc-service-api/api/person",
    "status": 500,
    "code": "Read timeout exception",
    "message": "Exception lors de l'appel à l'API PERSON"
}
```
Avec la trace ajoutée au flux sur les exceptions non prévues (= HTTP 500) :
```javascript
{
    "trace": [
        "declaringClass": "PersonService",
        "methodName": "readBySource",
        "fileName": "PersonService.java",
        "lineNumber": 506
    ]
}
```
Avec les erreurs ajoutées au flux sur les requests body incorrects (= HTTP 400) :
```javascript
{
    "errors": [
        "code": "field required",
        "objectName": "person",
        "field": "idPerson",
        "defaultMessage": "l'id Person est obligatoire"
    ]
}
```

## Log
Les logs d'erreur sont effectués sur le niveau WARN.

### Niveau de log (via actuator)
Connaître le niveau de log :  
GET {{protocol}}://{{hostname}}/{{service}}/actuator/loggers/{package ou classe}

Modifier le niveau de log :  
POST {{protocol}}://{{hostname}}/{{service}}/actuator/loggers/{package ou classe}

Body pour changer le niveau :
```javascript
{
    "configuredLevel": "WARN"
}
```

Body pour remettre sur le niveau de log par défaut :  
```javascript
{
    "effectiveLevel": "ERROR"
}
```

### Informations importantes
En production le niveau de log doit être positionné sur ERROR.

## Projets dépendants
- [postgresql-admin-back](https://github.com/flc-it/postgresql-admin-back)