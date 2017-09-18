/*
 * The MIT License
 *
 * Copyright (c) 2017, GrizzlyTech.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package testdomain.example;

import org.grizzlytech.protoxml.beans.Bean;
import org.grizzlytech.protoxml.util.NVP;
import testdomain.zoo.Animal;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class ExampleBean<T> {

    protected Class<T> clazz;

    protected List<JAXBElement<?>> thingList;

    protected JAXBElement<?> thing;

    protected List<? extends Animal> petList;

    protected NVP<Bean> beanHolder;

    public ExampleBean() {
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public JAXBElement<?> getThing() {
        return thing;
    }

    public void setThing(JAXBElement<?> thing) {
        this.thing = thing;
    }

    public List<JAXBElement<?>> getThingList() {
        return thingList;
    }

    public List<? extends Animal> getPetList() {
        return petList;
    }

    public NVP<Bean> getBeanHolder() {
        return beanHolder;
    }

    public void setBeanHolder(NVP<Bean> beanHolder) {
        this.beanHolder = beanHolder;
    }
}
