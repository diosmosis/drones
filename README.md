# drones

**drones** is a set of Java libraries that implements AngularJS features in Java for use in Android applications.

drones uses annotation processors extensively to do as much work at compile time as possible. For example, you can use
Angular-like XML templates (with embedded code & dynamic directives) to layout activites, but drones will not parse
the XML while your application runs.

Instead, the XML template will be parsed and compiled into a Java class that sets up the layout and behavior. This
provides the power and convenience of a framework like Angular w/o the cost in runtime performance (well, most of
it anyway).

## Status

Proof of concept: The current code works, but is not necessarily architected well and is not tested. These are both
immediate next steps.

## Features

The following features have been implemented:

* compile time processing of angular-like XML templates for View layouts
  * support for embedding java code in these templates.
  * support for global functions in embedded template code. (for the moment, replaces angular filters which are not supported)
  * string interpolation in templates
* angular scopes, watches & scope events for data binding and for pub/sub [Note: scope's are generated as classes, so
  accessing scope properties will be as fast as accessing object properties w/o reflection (since that is what happens))
* compile time processing of LESS files used to style Views
* Dagger DI integration (all classes in DI can be accessed in scopes)
* easy to use scrolling support via overflow-x/overflow-y LESS properties
* support for user defined directives
  * user defined directives can define their own scope properties, templates & LESS files
  * support for transclusion
  * directives can be matched w/ XML elements via attribute or tag
* support for dynamic directives (eg, ng-repeat)
* support for event directives (eg, ng-click)
* simple easy to use ViewGroups with an easy to use box model (based on the CSS box modeL). instead of a multitude of
  Layout classes and strange concepts like view gravity & weight, there are three ViewGroups defined that can be
  combined to any effect: <row>, <column>, <container>. Nesting them will probably be less efficient than using something like
  RelativeLayout, but it should be much easier to code and maintain.
* the ability to change activities through simple URLs instead of having to manually create Intents (in other words, routing)

## Example

The activity class:

```
package com.app.myapp.activities.homepage;

import com.flarestar.drones.base.annotations.Screen;
import com.flarestar.drones.mvw.annotations.*;
import com.flarestar.drones.base.BaseScreen;

@Screen
@Layout(value = "views/homepage.xml", stylesheet = "views/homepage.less") // both files are resources
public class Homepage extends BaseScreen {
    // there is no longer a need for code in our activity :)
}
```

The controller class:

```
package com.app.myapp.activities.homepage;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomepageController {

    private Context context;

    // The current activity will be injected automatically
    @Inject
    public HomepageController(Activity context) {
        this.context = context;
    }

    public void onCreateThingButtonClicked() {
        Toast.makeText(context, "create thing clicked", Toast.LENGTH_SHORT).show();
    }

    public void onSettingsButtonClicked() {
        Toast.makeText(context, "settings clicked", Toast.LENGTH_SHORT).show();
    }
}
```

The layout's XML template:

_homepage.xml_
```
<?xml version="1.0" encoding="UTF-8"?>
<!-- The '#' will ensure the class is injected through Dagger DI -->
<container id="root" ng-controller="#HomepageController as controller">
    <column id="main">
        <img id="logo" src="resource://drawable/logo"/>

        <button id="createThingButton" ng-click="controller.onCreateThingButtonClicked()">Create Thing</button>

        <button id="viewThingsButton" ng-click='$location.set("/com/app/myapp/activities/viewthings")'>View Things</button>

        <button id="settingsButton" ng-click="controller.onSettingsButtonClicked()">Settings</button>
    </column>
</container>
```

The layout's LESS template:

```
#root {
    #main {
        width: 100%;
    }

    img {
        width: 100%;
        margin-bottom: 90%;
    }

    button {
        width: 80%;

        margin-left: 50%;
        margin-right: 50%;

        margin-top: 3%;
    }

    button:last-child {
        margin-bottom: 1%;
    }
}
```
