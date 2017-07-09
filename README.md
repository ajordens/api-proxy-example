# api-proxy-example

A proof of concept api proxy built on top of Spring Boot.

## Purpose

[Spinnaker](https://www.spinnaker.io) aims to make it easy for users to integrate with pre-existing internal systems.

A common style of integration requires changes to both a Javascript UI and one or more JVM-based backend services.

This proxy eliminates the need for boiler plate APIs that do nothing more than marshall requests to/from the
[gateway service](https://www.spinnaker.io/reference/architecture/) and your internal system.

It supports:

- declarative configuration format (_no code needed!_)
- authentication via client certificates 
- proxying of GET requests (_POST forthcoming_) 

## Configuration

See `application.yml`

```
proxies:
  - id: flickr
    uri: https://api.flickr.com
  - id: spinnaker
    uri: https://api.spinnaker:7103
    keyStore: /path/to/my/keystore.jks
    keyStorePasswordFile: /path/to/my/password.txt
```

## Usage

##### Flickr Proxy

```

curl http://localhost:8080/proxies/flickr/services/feeds/photos_public.gne?format=json
 
{
  "status": "ok",
  "code": 200,
  "result": {
    "proxiedUrl": "https://api.flickr.com/services/feeds/photos_public.gne?format=json",
    "response": "jsonFlickrFeed(...)",
    "responseContentType": "application/javascript;"
  }
}
```

