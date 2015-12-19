package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeCreationCode;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.views.scope.Event;
import com.flarestar.drones.views.viewgroups.BaseDroneViewGroup;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * TODO
 *
 * TODO:
 * - remove Directive list from ViewNode
 */
public class MakeViewBody implements Renderable {
    private ScopeCreationCode scopeCreationCode;
    private ViewCreationCode viewCreationCode;
    private String text;
    private Collection<ScopeEventListener> events;
    private Collection<String> childViewIds;
    private boolean isDynamicViewGroup;
    private boolean isViewScopeView;
    private boolean hasTransclude;
    private boolean hasOwnScope;
    private Collection<WatcherDefinition> thisScopeWatchers;

    public MakeViewBody(ViewCreationCode viewCreationCode, String elementText, Collection<ScopeEventListener> events,
                        Collection<String> childViewIds, boolean isDynamicViewGroup, boolean isViewScopeView,
                        boolean hasTransclude, boolean hasOwnScope, ScopeCreationCode scopeCreationCode,
                        Collection<WatcherDefinition> thisScopeWatchers) {
        this.scopeCreationCode = scopeCreationCode;
        this.viewCreationCode = viewCreationCode;
        this.text = elementText;
        this.events = events;
        this.childViewIds = childViewIds;

        this.isDynamicViewGroup = isDynamicViewGroup;
        this.isViewScopeView = isViewScopeView;
        this.hasTransclude = hasTransclude;
        this.hasOwnScope = hasOwnScope;
        this.thisScopeWatchers = thisScopeWatchers;
    }

    @Override
    public String getTemplate() {
        return "templates/makeViewBody.twig";
    }

    @Override
    public String getModelAttribute() {
        return "body";
    }

    public boolean isViewScopeView() {
        return isViewScopeView;
    }

    public boolean hasTransclude() {
        return hasTransclude;
    }

    public ScopeCreationCode getScopeCreationCode() {
        return scopeCreationCode;
    }

    public boolean hasOwnScope() {
        return hasOwnScope;
    }

    public boolean hasText() {
        return text != null && !text.isEmpty();
    }

    public ViewCreationCode getViewCreationCode() {
        return viewCreationCode;
    }

    public String getText() {
        return text;
    }

    public Collection<ScopeEventListener> getEvents() {
        return events;
    }

    public Collection<String> getChildViewIds() {
        return childViewIds;
    }

    public boolean isDynamicViewGroup() {
        return isDynamicViewGroup;
    }

    public Collection<WatcherDefinition> getThisScopeWatchers() {
        return thisScopeWatchers;
    }
}
