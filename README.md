# Flavors #

To add a new flavour you need:

1. Copy exists folder `PROJECTDIR/app/src/byodl` to a new folder with new flavour name, for example `newbyodl`. So, we should copy all content of `PROJECTDIR/app/src/byodl` to `PROJECTDIR/app/src/newbyodl`

2. replace model file `PROJECTDIR/app/src/newbyodl/assets/striped_graph.pb` by your new model.

3. replace labels file `PROJECTDIR/app/src/newbyodl/assets/labels.txt` by your new labels file.
You should place one label/row

4. replace model file `PROJECTDIR/app/src/newbyodl/assets/version.txt` by file with model version in format `yyyymmddhhmmss` as it returned by backend

5. replace app icons files in `PROJECTDIR/app/src/newbyodl/res/mipmap*` folders

6. change app name and title in the file `PROJECTDIR/app/src/newbyodl/res/values/strings.xml`

7. add new flavour to `PROJECTDIR/app/build.gradle` file under the tag 

      ```
      productFlavors{
        byodl{
        ...
        }
        newbyodl {
          applicationId "anypackageidhere"
          signingConfig signingConfigs.release
        }
      }
      ```

8. Now, you need to edit some app configuration settings in the file `PROJECTDIR/app/src/newbyodl/java/com/byodl/FlavorConstants.java`
    * If you changed file names in pp2-4, please, change these name in the clsas `FlavourConstants.Model`
    * Update base link to an API in the `FlavourConstants.Api.BASE_URL`
    * Generally, you don't need change `FlavourConstants.Database.DB_NAME`

9. New flavour created

# Build apk from command line#

To build apk from command line you need:
Open `PROJECTDIR` and run: `gradlew assembleXxxxRelease`. Where Xxxx is a flavour name. For example, for flavour byodl you should run `gradlew assembleByodlRelease` for flavour `newbyodl` run `gradlew assembleNewbyodlRelease`

Apk file will be placed to `PROJECTDIR/app/build/output/apk` folder with name `App_xxxx_release.apk`, where xxxx - flavour name.