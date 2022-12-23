# Recognito
 User management & Authentication

 ## Description
 Recognito is designed to be a one stop shop for any application's user management and authorization. Each registered application is allocated their own unqiue RSA key pair. This key pair is used to encrypt and decrypt authorization tokens of users of said application. Users are also issued an 256 bit AES encrypted secret for loggin in purposes.
 Additionally users are also granted roles, and if permitted by the application resources (e.g. APIs) are locked behind roles, giving each user a tailored access as permitted by the admin of the application.

 ## Use Cases
 ### 1. Registering an Application

**Path** : */application*
**Request Body**
```
{
    "name": *Application name*
    "description": *Application description*
    "resourcesEnabled": *true/false whether to allow resource locking to roles*
}
```

**Flow**
![Label](relative link)

