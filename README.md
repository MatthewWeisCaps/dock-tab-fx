<!-- INTRO -->
## DockTabFx
This lightweight docking library provides an easy-to-use docking solution built on top of standard JavaFX components.

![docktabfx-demo-2x.gif](https://raw.githubusercontent.com/sireum/dock-tab-fx/master/docs/docktabfx-demo-2x.gif)

<!-- GETTING STARTED -->
## Getting Started

#### Maven
```maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.sireum</groupId>
        <artifactId>dock-tab-fx</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

#### Gradle
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'org.sireum:dock-tab-fx:1.1.0'
}
```

#### Sbt
```sbt
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "org.sireum" % "dock-tab-fx" % "1.1.0"
```

<!-- USAGE EXAMPLES -->
## Usage

Add a [DockablePane](https://github.com/sireum/dock-tab-fx/blob/master/src/main/java/org/sireum/docktabfx/DockablePane.java) 
to the scene and
[add tabs](https://github.com/sireum/dock-tab-fx/blob/b851394dae5a4cf49e5e8c68eb9fa2a57bfee1cc/src/main/java/org/sireum/docktabfx/DockablePane.java#L85) 
as needed. 

## Example

```java
public class Demo extends Application {

    @Override
    public void start(Stage stage) {
        final StackPane root = new StackPane();
        final DockablePane pane = new DockablePane();

        root.getChildren().add(pane);
        pane.addTab("tab01", new TextArea("tab 01"));
        pane.addTab("tab02", new Label("tab 02"));

        final Scene s = new Scene(root, 720, 480);
        stage.setScene(s);
        stage.show();
    }
}
```

For a hands-on demo, launch the
[interactive sandbox](https://github.com/sireum/dock-tab-fx/blob/master/src/test/java/org/sireum/docktabfx/ManyTabsSandbox.java)
via its
[launcher](https://github.com/sireum/dock-tab-fx/blob/master/src/test/java/org/sireum/docktabfx/ManyTabsSandboxLauncher.java).

<!-- LICENSE -->
## License
Licensed under [Apache Software License 2.0](www.apache.org/licenses/LICENSE-2.0)