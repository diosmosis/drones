package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.mvw.processing.renderables.scope.ScopeCreationCode;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopePropertyRenderable;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;

import java.util.Collection;

/**
 * TODO
 */
public class DirectiveMakeViewBody extends MakeViewBody {
    private boolean hasTranscludeDirective;
    private String isolateDirectiveName;
    private Collection<ScopePropertyRenderable> isolateDirectiveScopeProperties;

    public DirectiveMakeViewBody(ViewCreationCode viewCreationCode, String elementText, Collection<ScopeEventListener> events,
                                 Collection<String> childViewIds, boolean isDynamicViewGroup,
                                 boolean isScopeViewGroup, boolean hasTransclude, boolean hasOwnScope,
                                 ScopeCreationCode scopeCreationCode, Collection<WatcherDefinition> thisScopeWatchers,
                                 boolean hasTranscludeDirective, String isolateDirectiveName,
                                 Collection<ScopePropertyRenderable> isolateDirectiveScopeProperties) {
        super(viewCreationCode, elementText, events, childViewIds, isDynamicViewGroup, isScopeViewGroup, hasTransclude,
            hasOwnScope, scopeCreationCode, thisScopeWatchers);

        this.hasTranscludeDirective = hasTranscludeDirective;
        this.isolateDirectiveName = isolateDirectiveName;
        this.isolateDirectiveScopeProperties = isolateDirectiveScopeProperties;
    }

    @Override
    public String getTemplate() {
        return "templates/directiveMakeViewBody.twig";
    }

    public boolean hasTranscludeDirective() {
        return hasTranscludeDirective;
    }

    public String getIsolateDirectiveName() {
        return isolateDirectiveName;
    }

    public Collection<ScopePropertyRenderable> getIsolateDirectiveScopeProperties() {
        return isolateDirectiveScopeProperties;
    }
}
