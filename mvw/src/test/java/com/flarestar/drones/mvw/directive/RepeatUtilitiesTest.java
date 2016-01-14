package com.flarestar.drones.mvw.directive;

import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.scope.Scope;
import com.flarestar.drones.views.viewgroups.BaseDroneViewGroup;
import com.flarestar.drones.views.viewgroups.dynamic.RangeViewFactory;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static flarestar.bdd.Assert.expect;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(Runner.class)
@Describe(RepeatUtilities.class)
public class RepeatUtilitiesTest {

    private List<View> viewGroupViews;
    private Map<View, FakeScope> viewGroupScopes;
    private int makeViewCallCount;

    public void beforeEach() {
        viewGroupViews = new ArrayList<>();
        viewGroupScopes = new HashMap<>();
        makeViewCallCount = 0;
    }

    @Describe(desc = "#onValuesChanged()")
    public class OnValuesChangedTest {
        @It("should add new views to the ViewGroup if there are no views in the ViewGroup but values in the collection")
        public void testSuccess1() {
            BaseDroneViewGroup viewGroup = makeMockViewGroup();
            RangeViewFactory<Integer> viewFactory = new FakeRangeViewFactory<Integer>(1, 2, 3);

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(getViewGroupValues()).to().have().all().values(1, 2, 3);
            expect(getViewValue(viewFactory.getStartView())).to().be().equal(1);
            expect(getViewValue(viewFactory.getEndView())).to().be().equal(3);
            expect(makeViewCallCount).to().be().equal(3);
        }

        @It("should remove all views in the ViewGroup if there are views in the ViewGroup but no values in the collection")
        public void testSuccess2() {
            BaseDroneViewGroup viewGroup = makeMockViewGroup(1, 2, 3);
            RangeViewFactory<Integer> viewFactory = new FakeRangeViewFactory<>();

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(viewGroupViews).to().be().empty();
            expect(viewFactory.getStartView()).to().be().null_();
            expect(viewFactory.getEndView()).to().be().null_();
            expect(makeViewCallCount).to().be().equal(0);
        }

        @It("should replace all views in the ViewGroup w/ new views if there are views in the ViewGroup and values " +
            "in the collection, but nothing in common")
        public void testSuccess3() {
            BaseDroneViewGroup viewGroup = makeMockViewGroup(1, 2, 3);
            RangeViewFactory<Integer> viewFactory = new FakeRangeViewFactory<>(4, 5, 6);

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(getViewGroupValues()).to().have().all().values(4, 5, 6);
            expect(getViewValue(viewFactory.getStartView())).to().be().equal(4);
            expect(getViewValue(viewFactory.getEndView())).to().be().equal(6);
            expect(makeViewCallCount).to().be().equal(3);
        }

        @It("should remove views not in the value collection and add views for values not in the ViewGroup when the " +
            "ViewGroup & value collection share some values")
        public void testSuccess4() {
            BaseDroneViewGroup viewGroup = makeMockViewGroup(1, 2, 3);
            RangeViewFactory<Integer> viewFactory = new FakeRangeViewFactory<>(1, 2, 3, 4, 5, 6);

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(getViewGroupValues()).to().have().all().values(1, 2, 3, 4, 5, 6);
            expect(getViewValue(viewFactory.getStartView())).to().be().equal(1);
            expect(getViewValue(viewFactory.getEndView())).to().be().equal(6);
            expect(makeViewCallCount).to().be().equal(3);
        }

        @It("should move views around in the ViewGroup when the value collection doesn't contain new new values but " +
            "has a changed order")
        public void testSuccess7() {
            BaseDroneViewGroup viewGroup = makeMockViewGroup(1, 2, 3, 4, 5, 6);
            RangeViewFactory<Integer> viewFactory = new FakeRangeViewFactory<>(3, 4, 1, 2, 6, 5);

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(getViewGroupValues()).to().have().all().values(3, 4, 1, 2, 6, 5);
            expect(getViewValue(viewFactory.getStartView())).to().be().equal(3);
            expect(getViewValue(viewFactory.getEndView())).to().be().equal(5);
            expect(makeViewCallCount).to().be().equal(0);
        }

        @It("should recognize views with values that are equal to values in the collection, but are not the same objects")
        public void testSuccess6() {
            String value1 = "four";
            String value2 = new String("four");

            expect(value1).to().not().be().same(value2); // sanity check

            BaseDroneViewGroup viewGroup = makeMockViewGroup(1, "two", 3, value1, 5, "six");
            RangeViewFactory<Object> viewFactory = new FakeRangeViewFactory<Object>(3, value2, 4, "two", 1, 2, 6, 5, "six");

            RepeatUtilities.onValuesChanged(viewGroup, viewFactory);

            expect(getViewGroupValues()).to().have().all().values(3, "four", 4, "two", 1, 2, 6, 5, "six");
            expect(getViewValue(viewFactory.getStartView())).to().be().equal(3);
            expect(getViewValue(viewFactory.getEndView())).to().be().equal("six");
            expect(makeViewCallCount).to().be().equal(3);
        }
    }

    private List<Object> getViewGroupValues() {
        List<Object> result = new ArrayList<>();
        for (View view : viewGroupViews) {
            result.add(getViewValue(view));
        }
        return result;
    }

    private Object getViewValue(View view) {
        FakeScope scope = viewGroupScopes.get(view);
        return scope == null ? null : scope.item;
    }

    // mocks
    static class FakeScope extends Scope<Scope> {
        public Object item;
        public int location;

        public FakeScope(Object item, int location) {
            super(null, null);

            this.item = item;
            this.location = location;
        }
    }

    class FakeRangeViewFactory<T> extends RangeViewFactory<T> {
        public List<T> values;
        public View viewFactoryStartView;
        public View viewFactoryEndView;

        public FakeRangeViewFactory(T... initialValues) {
            values = Arrays.asList(initialValues);

            if (!viewGroupViews.isEmpty()) {
                viewFactoryStartView = viewGroupViews.get(0);
                viewFactoryEndView = viewGroupViews.get(viewGroupViews.size() - 1);
            }
        }

        @Override
        public View makeView(T _item, int _index) {
            ++makeViewCallCount;
            return makeMockView(_item, _index);
        }

        @Override
        public Iterable<T> getCollection() {
            return values;
        }

        @Override
        public T getItem(Scope scope) {
            FakeScope fakeScope = (FakeScope)scope;
            return (T)fakeScope.item;
        }

        @Override
        public void setScopeProperties(Scope scope, Object item, int index) {
            FakeScope fakeScope = (FakeScope)scope;
            fakeScope.item = item;
            fakeScope.location = index;
        }

        @Override
        public View getStartView() {
            return viewFactoryStartView;
        }

        @Override
        public View getEndView() {
            return viewFactoryEndView;
        }

        @Override
        public void setEndView(View endView) {
            viewFactoryEndView = endView;
        }

        @Override
        public void setStartView(View startView) {
            viewFactoryStartView = startView;
        }

        @Override
        public void makeViews(ViewGroup parent) {
            super.makeViews(parent);
        }
    }

    public BaseDroneViewGroup makeMockViewGroup(Object... initialValues) {
        setInitialViewGroupValues(initialValues);

        BaseDroneViewGroup result = mock(BaseDroneViewGroup.class);
        final Scope mockScope = makeMockParentScope();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                View view = (View)invocationOnMock.getArguments()[0];
                int location = (int)invocationOnMock.getArguments()[1];

                viewGroupViews.add(location, view);
                return null;
            }
        }).when(result).addView(any(View.class), anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                int location = (int)invocationOnMock.getArguments()[0];
                return (location >= viewGroupViews.size() || location < 0) ? null : viewGroupViews.get(location);
            }
        }).when(result).getChildAt(anyInt());

        when(result.getScope()).thenReturn(mockScope);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                int from = (int)invocationOnMock.getArguments()[0];
                int to = (int)invocationOnMock.getArguments()[1];

                View fromView = viewGroupViews.remove(from);
                viewGroupViews.add(to, fromView);
                return null;
            }
        }).when(result).moveView(anyInt(), anyInt());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                int location = (int)invocationOnMock.getArguments()[0];
                viewGroupViews.remove(location);
                return null;
            }
        }).when(result).removeViewAt(anyInt());

        when(result.getChildCount()).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return viewGroupViews.size();
            }
        });

        return result;
    }

    private void setInitialViewGroupValues(Object[] initialValues) {
        for (int i = 0; i != initialValues.length; ++i) {
            viewGroupViews.add(makeMockView(initialValues[i], i));
        }
    }

    private Scope makeMockParentScope() {
        Scope result = mock(Scope.class);
        when(result.getChildScopeFor(any(View.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                View view = (View)invocationOnMock.getArguments()[0];
                return viewGroupScopes.get(view);
            }
        });
        return result;
    }

    private View makeMockView(Object item, int index) {
        View result = mock(View.class);

        FakeScope viewScope = new FakeScope(item, index);
        viewGroupScopes.put(result, viewScope);

        return result;
    }
}
