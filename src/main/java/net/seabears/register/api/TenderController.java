package net.seabears.register.api;

import net.seabears.register.core.DataStore;
import net.seabears.register.core.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tender")
public class TenderController {
    @Autowired
    private DataStore data;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void pay(@RequestBody Payment payment) {
        data.createPayment(payment);
    }
}

