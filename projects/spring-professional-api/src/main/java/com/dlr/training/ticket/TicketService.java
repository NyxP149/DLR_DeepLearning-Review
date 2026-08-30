package com.dlr.training.ticket;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {
    public List<Ticket> findAll() {
        return List.of(new Ticket(1, "Mesurer le pipeline", Ticket.Status.OPEN));
    }
}
