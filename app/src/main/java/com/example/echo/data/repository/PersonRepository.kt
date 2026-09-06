package com.example.echo.data.repository

import com.example.echo.data.local.InMemoryDataStore
import com.example.echo.domain.model.Person
import kotlinx.coroutines.flow.Flow

class PersonRepository(private val store: InMemoryDataStore) {

    fun getAllPersons(): Flow<List<Person>> = store.persons

    suspend fun getPersonById(id: String): Person? =
        store.getPersonById(id)

    suspend fun insertPerson(person: Person) =
        store.insertPerson(person)

    suspend fun updatePerson(person: Person) =
        store.updatePerson(person)
}
