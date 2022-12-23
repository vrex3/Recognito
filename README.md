# Recognito
 User management & Authentication

 ## Description
 Recognito is designed to be a one stop shop for any application's user management and authorization. Each registered application is allocated their own unqiue RSA key pair. This key pair is used to encrypt and decrypt authorization tokens of users of said application. Users are also issued an 256 bit AES encrypted secret for loggin in purposes.
 Additionally users are also granted roles, and if permitted by the application resources (e.g. APIs) are locked behind roles, giving each user a tailored access as permitted by the admin of the application.

## Architecture

![Recognito drawio](https://user-images.githubusercontent.com/29248886/209383376-215e9a07-8af0-44f7-bf93-dac4296fdaff.png)

 

 ## Use Cases
 ### 1. Registering an Application

**Path** : */application*
**Request Body**
```
{
    "name": Application name
    "description": Application description
    "resourcesEnabled": true/false whether to allow resource locking to roles
}
```

**Flow**

![Upsert_Application drawio](https://user-images.githubusercontent.com/29248886/209365537-450a875f-f489-4e26-8f49-9e36193a8ea2.png)

