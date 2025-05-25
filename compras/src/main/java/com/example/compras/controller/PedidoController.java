package com.example.compras.controller;

import com.example.compras.model.Pedido;
import com.example.compras.model.ItemPedido;
import com.example.compras.repository.PedidoRepository;
import com.example.compras.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // GET /pedidos → Listar todos os pedidos
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByDataDesc();
        return ResponseEntity.ok(pedidos);
    }

    // GET /pedidos/{id} → Obter um pedido específico
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);
        
        if (pedido.isPresent()) {
            return ResponseEntity.ok(pedido.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        try {

            if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
                return ResponseEntity.badRequest().build();
            }

            if (!clienteRepository.existsById(pedido.getCliente().getId())) {
                return ResponseEntity.badRequest().build();
            }

            if (pedido.getData() == null) {
                pedido.setData(LocalDateTime.now());
            }

            if (pedido.getItens() != null) {
                for (ItemPedido item : pedido.getItens()) {
                    item.setPedido(pedido);
                }
            }

            Pedido pedidoSalvo = pedidoRepository.save(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedidoSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizarPedido(@PathVariable Long id, @RequestBody Pedido pedidoAtualizado) {
        try {
            Optional<Pedido> pedidoExistente = pedidoRepository.findById(id);
            
            if (!pedidoExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            pedidoAtualizado.setId(id);

            if (pedidoAtualizado.getCliente() != null && pedidoAtualizado.getCliente().getId() != null) {
                if (!clienteRepository.existsById(pedidoAtualizado.getCliente().getId())) {
                    return ResponseEntity.badRequest().build();
                }
            }

            // Configurar relacionamento bidirecional com itens
            if (pedidoAtualizado.getItens() != null) {
                for (ItemPedido item : pedidoAtualizado.getItens()) {
                    item.setPedido(pedidoAtualizado);
                }
            }

            Pedido pedidoSalvo = pedidoRepository.save(pedidoAtualizado);
            return ResponseEntity.ok(pedidoSalvo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPedido(@PathVariable Long id) {
        try {
            if (!pedidoRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            pedidoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}