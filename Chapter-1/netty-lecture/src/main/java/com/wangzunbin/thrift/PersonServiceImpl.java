package com.wangzunbin.thrift;

import org.apache.thrift.TException;

import thrift.generated.DataException;
import thrift.generated.Person;
import thrift.generated.PersonService;

public class PersonServiceImpl implements PersonService.Iface {
    @Override
    public Person getPersonByUsername(String username) throws DataException, TException {
        System.out.println("Got Client Param: " + username);
        Person person = new Person();
        person.setUsername(username);
        person.setAge(22);
        person.setMarried(false);

        System.out.println("getPersonByUsername success:"+person.getUsername());
        return person;
    }

    @Override
    public void savePerson(Person person) throws DataException, TException {
        System.out.println("Got Client Param: ");

        System.out.println(person.getUsername());
        System.out.println(person.getAge());
        System.out.println(person.isMarried());

        System.out.println("Got Client savePerson success!! ");
    }
}
