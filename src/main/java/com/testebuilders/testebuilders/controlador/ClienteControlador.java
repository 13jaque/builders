package com.testebuilders.testebuilders.controlador;


import com.testebuilders.testebuilders.entidade.Cliente;
import com.testebuilders.testebuilders.repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@RestController
@RequestMapping("/clientes")
public class ClienteControlador {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> get(@PathVariable String id) {
        Optional<Cliente> resp = clienteRepositorio.findById(id);

        if (resp.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        resp.get().setIdade(calcularIdade(resp.get().getDataNascimento()));

        return new ResponseEntity<>(resp.get(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> post( @RequestBody Cliente cliente) {
        if (!eValida(cliente.getDataNascimento())) {
            return new ResponseEntity<String>("DataNascimento inexistente ou inválida", HttpStatus.BAD_REQUEST);
        }

        cliente.setId(UUID.randomUUID().toString());
        clienteRepositorio.save(cliente);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable String id) {
        clienteRepositorio.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Cliente newCliente){
        if (!eValida(newCliente.getDataNascimento())) {
            return new ResponseEntity<String>("DataNascimento inexistente ou inválida", HttpStatus.BAD_REQUEST);
        }

        Optional<Cliente> oldCliente = clienteRepositorio.findById(id);
        if (oldCliente.isPresent()) {
            Cliente cliente = oldCliente.get();
            cliente.setNome(newCliente.getNome());
            cliente.setCpf(newCliente.getCpf());
            cliente.setDataNascimento(newCliente.getDataNascimento());
            clienteRepositorio.save(cliente);
            return new ResponseEntity<>(HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping ("/{id}")
    public ResponseEntity patch(@PathVariable String id, @RequestBody Cliente newCliente){
        Optional<Cliente> oldCliente = clienteRepositorio.findById(id);
        if (oldCliente.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cliente cliente = oldCliente.get();
        if (newCliente.getNome() != null) {
            cliente.setNome(newCliente.getNome());
        }

        if (newCliente.getCpf() != null) {
            cliente.setCpf(newCliente.getCpf());
        }

        if(newCliente.getDataNascimento() != null) {
            cliente.setDataNascimento((newCliente.getDataNascimento()));
        }

        clienteRepositorio.save(cliente);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<Page<Cliente>> get(@ModelAttribute ClienteRequest clienteRequest) {

        PageRequest pageRequest = PageRequest.of(clienteRequest.getPagina(), clienteRequest.getLimite());
        Cliente cliente = new Cliente();
        cliente.setCpf(clienteRequest.getCpf());
        cliente.setNome(clienteRequest.getNome());

        Page<Cliente> clientePage = clienteRepositorio.findAll(Example.of(cliente), pageRequest);
        for (Cliente c : clientePage.getContent()){
            c.setIdade(calcularIdade(c.getDataNascimento()));
        }

        return new ResponseEntity<>(clientePage, HttpStatus.OK);
    }

    private int calcularIdade(LocalDate dataNascimento) {
        final LocalDate dataAtual = LocalDate.now();
        final Period periodo = Period.between(dataNascimento, dataAtual);
        return periodo.getYears();
    }

    private boolean eValida(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            return false;
        }else if (!dataNascimento.isBefore(LocalDate.now())){
            return false;
        }

        return true;
    }

   public class ClienteRequest{
        private String nome;
        private String cpf;
        private int pagina;
        private int limite;

        public String getNome(){
            return nome;
        }

        public void setNome(String nome){
            this.nome = nome;
        }

        public String getCpf(){
            return cpf;
        }
        public void setCpf(String cpf){
            this.cpf = cpf;
        }

       public int getPagina() {
           return pagina;
       }

       public void setPagina(int pagina) {
           this.pagina = pagina;
       }

       public int getLimite() {
           if (limite == 0) {
               limite = 10;
           }

           return limite;
       }

       public void setLimite(int limite) {
           this.limite = limite;
       }
   }
}
