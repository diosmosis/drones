package com.flarestar.drones.base.inject;

import com.google.inject.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import org.junit.runner.RunWith;

import static flarestar.bdd.Assert.expect;

@RunWith(Runner.class)
@Describe(GuiceFactoryDelegate.class)
public class GuiceFactoryDelegateTest {

    public static abstract class BaseType {
        public int value;

        public BaseType(int value) {
            this.value = value;
        }
    }

    @InstanceFactory(InstanceType.Factory.class)
    public static class InstanceType extends BaseType {
        public InstanceType(int value) {
            super(value);
        }

        @AssistedInject
        public InstanceType(@Assisted Object[] args) {
            this((int)args[0]);
        }

        interface Factory extends GenericInstanceFactory<InstanceType> {
        }
    }

    public static class TypeWithoutInstanceFactory extends  BaseType {
        public TypeWithoutInstanceFactory(int value) {
            super(value);
        }
    }

    @InstanceFactory(TypeWithFailingGenericInstanceFactory.Factory.class)
    public static class TypeWithFailingGenericInstanceFactory extends BaseType {
        public TypeWithFailingGenericInstanceFactory(int value) {
            super(value);
        }

        @AssistedInject
        public TypeWithFailingGenericInstanceFactory(@Assisted Object[] args) {
            this((int)args[0]);
        }

        interface Factory extends GenericInstanceFactory<TypeWithFailingGenericInstanceFactory> {
        }
    }

    public static class DroneModule extends AbstractModule {
        @Override
        protected void configure() {
            Key<InstanceType.Factory> key = Key.get(InstanceType.Factory.class, Names.named(InstanceType.class.getName()));
            install(new FactoryModuleBuilder()
                .implement(BaseType.class, InstanceType.class)
                .build(key));
        }
    }

    private DroneModule module;
    private Injector injector;
    private GuiceFactoryDelegate instance;

    public void beforeEach() {
        this.module = new DroneModule();
        this.injector = Guice.createInjector(module);
        this.instance = injector.getInstance(GuiceFactoryDelegate.class);
    }

    @Describe(desc = "#make()")
    public class MakeTest {
        @It("should throw an exception if the target class is not annotated with @InstanceFactory")
        public void testFailure() {
            expect(new Runnable() {
                @Override
                public void run() {
                    instance.make(TypeWithoutInstanceFactory.class, 20);
                }
            }).to().throw_(IllegalArgumentException.class);
        }

        @It("should trigger a Guice failure if the associated GenericInstanceFactory is not bound")
        public void testFailure2() {
            expect(new Runnable() {
                @Override
                public void run() {
                    instance.make(TypeWithFailingGenericInstanceFactory.class, 30);
                }
            }).to().throw_(Throwable.class);
        }

        @It("should trigger an error if we pass the wrong arguments to the GenericInstanceFactory.make method")
        public void testFailure3() {
            expect(new Runnable() {
                @Override
                public void run() {
                    instance.make(InstanceType.class, "not an int");
                }
            }).to().throw_(ClassCastException.class);
        }

        @It("should create an instance using the associated GenericInstanceFactory when it can be created")
        public void testSuccess() {
            InstanceType created = instance.make(InstanceType.class, 40);
            expect(created.value).to().be().equal(40);
        }
    }
}
