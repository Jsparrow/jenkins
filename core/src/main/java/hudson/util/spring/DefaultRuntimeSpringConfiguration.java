/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hudson.util.spring;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A programmable runtime Spring configuration that allows a spring ApplicationContext
 * to be constructed at runtime
 *
 * Credit must go to Solomon Duskis and the
 * article: http://jroller.com/page/Solomon?entry=programmatic_configuration_in_spring
 *
 * @author Graeme
 * @since 0.3
 *
 */
class DefaultRuntimeSpringConfiguration implements RuntimeSpringConfiguration {
    private static final Logger LOGGER = Logger.getLogger(DefaultRuntimeSpringConfiguration.class.getName());
    private StaticWebApplicationContext context;
    private Map<String,BeanConfiguration> beanConfigs = new HashMap<>();
    private Map<String,BeanDefinition> beanDefinitions = new HashMap<>();
    private List<String> beanNames = new ArrayList<>();

    public DefaultRuntimeSpringConfiguration() {
        this.context = new StaticWebApplicationContext();
    }

    public DefaultRuntimeSpringConfiguration(ApplicationContext parent) {
        this.context = new StaticWebApplicationContext();
        context.setParent(parent);
//        if(parent != null){
//            trySettingClassLoaderOnContextIfFoundInParent(parent);
//        }
    }

//    private void trySettingClassLoaderOnContextIfFoundInParent(ApplicationContext parent) {
//        try{
//            Object classLoader = parent.getBean(GrailsRuntimeConfigurator.CLASS_LOADER_BEAN);
//            if(classLoader instanceof ClassLoader){
//            //    this.context.setClassLoader((ClassLoader) classLoader);
//            }
//        }catch(NoSuchBeanDefinitionException nsbde){
//            //ignore, we tried our best
//        }
//    }


    @Override
	public BeanConfiguration addSingletonBean(String name, Class clazz) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name,clazz);
        registerBeanConfiguration(name, bc);
        return bc;
    }

    @Override
	public BeanConfiguration addPrototypeBean(String name, Class clazz) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name,clazz,true);
        registerBeanConfiguration(name, bc);
        return bc;
    }

    @Override
	public WebApplicationContext getApplicationContext() {
        registerBeansWithContext(context);
        context.refresh();
        return context;
    }

    @Override
	public WebApplicationContext getUnrefreshedApplicationContext() {
        return context;
    }

    @Override
	public BeanConfiguration addSingletonBean(String name) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name);
        registerBeanConfiguration(name, bc);
        return bc;
    }

    @Override
	public BeanConfiguration createSingletonBean(Class clazz) {
        return new DefaultBeanConfiguration(clazz);
    }

    @Override
	public BeanConfiguration addSingletonBean(String name, Class clazz, Collection args) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name,clazz,args);
        registerBeanConfiguration(name, bc);
        return bc;
    }

    @Override
	public BeanConfiguration addPrototypeBean(String name) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name,true);
        registerBeanConfiguration(name, bc);
        return bc;
    }

    private void registerBeanConfiguration(String name, BeanConfiguration bc) {
        beanConfigs.put(name,bc);
        beanNames.add(name);
    }

    @Override
	public BeanConfiguration createSingletonBean(Class clazz, Collection constructorArguments) {
        return new DefaultBeanConfiguration(clazz, constructorArguments);
    }

    @Override
	public void setServletContext(ServletContext context) {
        this.context.setServletContext(context);
    }

    @Override
	public BeanConfiguration createPrototypeBean(String name) {
        return new DefaultBeanConfiguration(name,true);
    }

    @Override
	public BeanConfiguration createSingletonBean(String name) {
        return new DefaultBeanConfiguration(name);
    }

    @Override
	public void addBeanConfiguration(String beanName, BeanConfiguration beanConfiguration) {
        beanConfiguration.setName(beanName);
        registerBeanConfiguration(beanName, beanConfiguration);
    }

    @Override
	public void addBeanDefinition(String name, BeanDefinition bd) {
        beanDefinitions.put(name,bd);
        beanNames.add(name);
    }

    @Override
	public boolean containsBean(String name) {
        return beanNames .contains(name);
    }

    @Override
	public BeanConfiguration getBeanConfig(String name) {
        return beanConfigs.get(name);
    }

    @Override
	public AbstractBeanDefinition createBeanDefinition(String name) {
        if(containsBean(name)) {
            if(beanDefinitions.containsKey(name)) {
				return (AbstractBeanDefinition)beanDefinitions.get(name);
			} else if(beanConfigs.containsKey(name)) {
				return beanConfigs.get(name).getBeanDefinition();
			}
        }
        return null;
    }

    @Override
	public void registerPostProcessor(BeanFactoryPostProcessor processor) {
        this.context.addBeanFactoryPostProcessor(processor);
    }



    @Override
	public List<String> getBeanNames() {
        return beanNames;
    }

    @Override
	public void registerBeansWithContext(StaticApplicationContext applicationContext) {
        for (BeanConfiguration bc : beanConfigs.values()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(new StringBuilder().append("[RuntimeConfiguration] Registering bean [").append(bc.getName()).append("]").toString());
                if (LOGGER.isLoggable(Level.FINEST)) {
                    PropertyValue[] pvs = bc.getBeanDefinition()
                            .getPropertyValues()
                            .getPropertyValues();
                    for (PropertyValue pv : pvs) {
                        LOGGER.finest(new StringBuilder().append("[RuntimeConfiguration] With property [").append(pv.getName()).append("] set to [").append(pv.getValue()).append("]")
								.toString());
                    }
                }
            }


            if (applicationContext.containsBeanDefinition(bc.getName())) {
				applicationContext.removeBeanDefinition(bc.getName());
			}

            applicationContext.registerBeanDefinition(bc.getName(),
                    bc.getBeanDefinition());
        }
        for (String key : beanDefinitions.keySet()) {
            BeanDefinition bd = beanDefinitions.get(key);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(new StringBuilder().append("[RuntimeConfiguration] Registering bean [").append(key).append("]").toString());
                if (LOGGER.isLoggable(Level.FINEST)) {
                    for (PropertyValue pv : bd.getPropertyValues().getPropertyValues()) {
                        LOGGER.finest(new StringBuilder().append("[RuntimeConfiguration] With property [").append(pv.getName()).append("] set to [").append(pv.getValue()).append("]")
								.toString());
                    }
                }
            }
            if (applicationContext.containsBean(key)) {
                applicationContext.removeBeanDefinition(key);
            }

            applicationContext.registerBeanDefinition(key, bd);

        }
    }

    /**
     * Adds an abstract bean and returns the BeanConfiguration instance
     *
     * @param name The name of the bean
     * @return The BeanConfiguration object
     */
    @Override
	public BeanConfiguration addAbstractBean(String name) {
        BeanConfiguration bc = new DefaultBeanConfiguration(name);
        bc.setAbstract(true);
        registerBeanConfiguration(name, bc);

        return bc;
    }
}
