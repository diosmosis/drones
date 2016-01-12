package com.flarestar.drones.base.discovery;

import android.app.Activity;
import com.flarestar.drones.base.BaseScreen;
import com.flarestar.drones.base.Drone;
import com.flarestar.drones.base.DroneCollection;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;

import static flarestar.bdd.Assert.expect;

@RunWith(Runner.class)
@Describe(DroneSniffer.class)
public class DroneSnifferTest {

    public static class TestActivityWithoutComponent extends BaseScreen {
    }

    public static class TestActivityWithBrokenComponent extends BaseScreen {
    }

    public static class TestActivityWithBrokenComponentActivityComponent {
    }

    public static class TestActivityWithBrokenComponent2 extends BaseScreen {
    }

    public static class TestActivityWithBrokenComponent2ActivityComponent {
        public static DroneCollection build(Activity activity) {
            return null;
        }
    }

    public static class TestActivityWithBrokenComponent3 extends BaseScreen {
    }

    public static class TestActivityWithBrokenComponent3ActivityComponent {
        public DroneCollection build(Activity activity) {
            return null;
        }
    }

    public static class TestActivityWithWorkingComponent extends BaseScreen {}

    public static class TestActivityWithWorkingComponentActivityComponent {
        public static DroneCollection build(Activity activity) {
            return new DroneCollection() {
                @Override
                public Drone[] getDrones() {
                    return new Drone[0];
                }
            };
        }
    }

    private DroneSniffer instance;

    public void beforeEach() {
        this.instance = new DroneSniffer();
    }

    @It("should throw a runtime exception when the ActivityComponent class does not exist")
    public void testFindDroneCollectionForFailure1() {
        expect(new Runnable() {
            @Override
            public void run() {
                instance.findDroneCollectionFor(mock(TestActivityWithoutComponent.class),
                    TestActivityWithoutComponent.class.getName() + "ActivityComponent");
            }
        }).to().throw_(RuntimeException.class, "ClassNotFoundException");
    }

    @It("should throw a runtime exception when the ActivityComponent class exists, but has no .build() method")
    public void testFindDroneCollectionForFailure2() {
        expect(new Runnable() {
            @Override
            public void run() {
                instance.findDroneCollectionFor(mock(TestActivityWithBrokenComponent.class),
                    TestActivityWithBrokenComponent.class.getName() + "ActivityComponent");
            }
        }).to().throw_(RuntimeException.class, "java.lang.NoSuchMethodException: com.flarestar.drones.base.discovery.DroneSnifferTest$TestActivityWithBrokenComponentActivityComponent.build(android.app.Activity)");
    }

    @It("should throw a runtime exception when the ActivityComponent returns a null DroneCollection")
    public void testFindDroneCollectionForFailure3() {
        expect(new Runnable() {
            @Override
            public void run() {
                instance.findDroneCollectionFor(mock(TestActivityWithBrokenComponent2.class),
                    TestActivityWithBrokenComponent2.class.getName() + "ActivityComponent");
            }
        }).to().throw_(RuntimeException.class, "Invalid ActivityComponent.build() method");
    }

    @It("should throw a runtime exception when the ActivityComponent's build method is non-static")
    public void testFindDroneCollectionForFailure4() {
        expect(new Runnable() {
            @Override
            public void run() {
                instance.findDroneCollectionFor(mock(TestActivityWithBrokenComponent3.class),
                    TestActivityWithBrokenComponent3.class.getName() + "ActivityComponent");
            }
        }).to().throw_(RuntimeException.class, "Invalid ActivityComponent.build() method: build method must be static.");
    }

    @It("should not throw when a working ActivityComponent's build method is found and invoked")
    public void testFindDroneCollectionForSuccess() {
        DroneCollection result = instance.findDroneCollectionFor(mock(TestActivityWithWorkingComponent.class),
            TestActivityWithWorkingComponent.class.getName() + "ActivityComponent");

        expect(result).to().not().be().null_();
    }
}
