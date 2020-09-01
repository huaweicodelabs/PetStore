# PetStore


## Table of Contents

 * [Introduction](#introduction)
 * [Preparation](#preparation)
 * [Installation](#installation)
 * [Experience Different Functions](#function-use)
 * [License](#license)


## Introduction
 The sample code describes how to use the HMS Account Kit、In-App-Purchase Kit、Push Kit、Map Kit、Location Kit and some other HMS Services, including a PetStore App and its corresponding server.
 In order to facilitate readers to have a more comprehensive understanding of how to integrate the various capabilities of HMS Core, we have planned the following functions for the PetStore App:
 * Account Registration: registered user name and password;
 * System Login: support user name password login, fingerprints, and the third party to login;
 * Personal Center Settings: user login after entering personal center, you can set the shipping address, log in and set fingerprint;
 * Browsing Nearby PetStores: support to view detailed address of PetStore, search and route search, etc.;
 * Buy Membership: support member view of commodity, commodity purchase and the purchase order to see;
 * Watch Video: pet support browse a list of video and video playback;
 * News push: that can receive push pet related information to the users of developers.

## Preparation
### 1. Register as a developer.
    Before you get started, you must register as a HUAWEI developer and complete identity verification on [HUAWEI Developers](https://developer.huawei.com/consumer/en/). For details, please refer to [Registration and Verification](https://developer.huawei.com/consumer/en/doc/start/10104).
### 2. Create an app and apply for a agconnect-services.json.
    Create an app and set package type to APK (android app). Apply for the agconnect-services.json file on HUAWEI Developers. For details, please refer to [Adding the AppGallery Connect Configuration File].
    See details: [HUAWEI Account Kit Development Preparation](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/config-agc-0000001050196065-V5)
### 3. Build
    To build the HMSPetStoreApp sample Code, import the sample to Android Studio (3.x +), download agconnect-services.json from AppGallery Connect, and add the file to the app's root directory app of the demo app. Config the API Key and Public Key of IAP Information in petstore_cofing.xml. 
    To build the HMSPetstoreServer sample Code, import the sample to Intellij IDEA, and config AppId and AppSecret in PushService.java、AtDemo.java and UserRisksVerifyHandler.java, and config public key of IAP information in AppServer.java. And then you can deploy the server as instructed.

## Installation
    Download the sample code and open it in Android Studio. Ensure that your device has been connected to the internet and obtain the APK by building a project.

## Function Use
    You can tap buttons in your app to experience rich services of HMS Services. And you can download the apk through the following Qr code.
![image](https://github.com/huaweicodelabs/PetStore/blob/master/HMSPetStoreApp/QR%20Code%20Of%20HMSPetStoreApp.png)

## License
    The sample of PetStore has obtained the [Apache 2.0 license.](http://www.apache.org/licenses/LICENSE-2.0).
