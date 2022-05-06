# Simple Playframework CRUD 

This repository contains an MVC 2-tier web application and showcases some capabilities of [Play Framework](https://www.playframework.com/).

This is provided as part of qualification process for employment by [Iran Book and Literature House](https://ketab.ir/), though is by no means affiliated to, nor the developer is endorsed by them.

## Build and Run

SDKMAN is required for building and running this project. After properly [installing](https://sdkman.io/install) it, clone the repository then execute the following inside repository's root folder:

    $ sdk env install
    $ sdk env
    $ sbt
    [simple-playframework-crud] $ run
The application assumes an up and running instance of PostgreSQL DBMS with a working database per the connection configured within `./conf/application.conf`, whose schema is per `./assets/db/schema.sql`, and populated by `./assets/db/dataset.sql`.

## Usage

Head to [http://127.0.0.1:9000](http://127.0.0.1:9000) and a list of available CRUDs shall be shown.

## Notes

- A working internet connection is needed to get Bootstrap from online CDN on first run
- CORS may be needed to be disabled on your browser
- Reclone the repository into a new local folder if you are coming from a past release (i.e. due to Git history rewrites, a `git pull` will pose several merge conflicts)
