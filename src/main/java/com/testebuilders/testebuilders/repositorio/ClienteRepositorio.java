package com.testebuilders.testebuilders.repositorio;

import com.testebuilders.testebuilders.entidade.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteRepositorio extends MongoRepository<Cliente, String>{}
