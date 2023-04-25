package com.example.springcashier;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")

public class SpringCashierController {
    private final OrderQueryRepository orderRepository;
    private final OrderModelRepository orderModelRepository;

    @Autowired
    public SpringCashierController(OrderQueryRepository orderRepository, OrderModelRepository orderModelRepository) {
        this.orderRepository = orderRepository;
        this.orderModelRepository = orderModelRepository;
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    private @Value("${hmac.key}") String key;

    private String hmac_sha256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(data.getBytes());
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            return encoder.encodeToString(digest);
        } catch (InvalidKeyException e1) {
            throw new RuntimeException("Invalid key exception while converting to HMAC SHA256");
        } catch (NoSuchAlgorithmException e2) {
            throw new RuntimeException("Java Exception Initializing HMAC Crypto Algorithm");
        }
    }

    // Database Response Debugger
    @PostMapping("/selectedStore")
    public ResponseEntity<?> setSelectedStore(@RequestParam("register") String register, HttpSession session) {
        if (register != null && !register.isEmpty()) {
            // Save the register value in the session
            session.setAttribute("selectedRegister", register);
        } else {
            // Get the register value from the session
            register = (String) session.getAttribute("selectedRegister");
        }
        return ResponseEntity.ok().build();
    }

    //     GET MAPPING
    @GetMapping
    public String getAction(@ModelAttribute("command") Command command,
                            Model model, HttpSession session) {
        // Get the register value from the session attribute
        command.setRegister("5012349");

        // Move the setSelectedStore() method call to the getAction() method
//        setSelectedStore(command.getRegister(), session);

        String message = "Starbucks Reserved Order" + "\n\n" +
                "Register: " + command.getRegister() + "\n" +
                "Status:   " + "Ready for New Order" + "\n";

        String state = Order.GetNewOrder().getClass().getName();
        command.setState(state);
        saveOrder(Order.GetNewOrder());

        long ts_long = System.currentTimeMillis();
        String ts_string = String.valueOf(ts_long);
        command.setTimestamp(ts_string);

        String text = state + "/" + ts_string;
        String hash_string = hmac_sha256(key, text);
        command.setHash(hash_string);

        String server_ip;
        String host_name;

        try {
            InetAddress ip = InetAddress.getLocalHost();
            server_ip = ip.getHostAddress();
            host_name = ip.getHostName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("hash", hash_string);
        model.addAttribute("message", message);
        model.addAttribute("server", host_name + "/" + server_ip);

        return "starbucks";
    }
//     POST MAPPING

    @PostMapping
    public String postAction(
            @Valid @ModelAttribute("command") Command command,
            @RequestParam(value = "action", required = true) String action,
            Errors errors, Model model, HttpServletRequest request, HttpSession session) {


        // Get the register value from the request parameters
        command.setRegister(request.getParameter("register"));
        if (request.getParameter("register") != null) {
            command.setRegister(request.getParameter("register"));
        }

        if (request.getParameter("register") != null) {
            command.setRegister(request.getParameter("register"));
        }
        if (errors.hasErrors()) {
            return "starbucks";
        }

        String message = "";


        // returning the hash
        String input_hash = command.getHash();
        String input_state = command.getState();
        String input_timestamp = command.getTimestamp();

        String input_text = input_state + "/" + input_timestamp;
        String calculated_hash = hmac_sha256(key, input_text);

        log.info("Input Hash: " + input_hash);
        log.info("Valid Hash: " + calculated_hash);

        // check message integrity
        if (!input_hash.equals(calculated_hash)) {
            throw new CashierServerError();
        }

        long ts1 = Long.parseLong(input_timestamp);
        long ts2 = System.currentTimeMillis();
        long diff = ts2 - ts1;

        log.info("Input Timestamp: " + ts1);
        log.info("Current Timestamp: " + ts2);
        log.info("Timestamp Delta: " + diff);

        // guard against replay attack
        if ((diff / 1000) > 1000) {
            throw new CashierServerError();
        }
        /* Process Post Action */
        switch (action) {
            // Place Order
            case "Place Order":
                // Check if an order is already active for this register
                boolean activeOrderExists = false;
                for (Order existingOrder : orderModelRepository.findByRegister(String.valueOf(Long.valueOf(command.getRegister())))) {
                    if (existingOrder.getStatus().equals("Active")) {
                        activeOrderExists = true;
                        break;
                    }
                }
                if (activeOrderExists) {
                    // An active order already exists, so we cannot place a new order
                    message = "An active order already exists for this register. Please clear the existing order before placing a new one.";
                } else {
                    // No active order exists, so we can create a new order
                    Order order = Order.GetNewOrder();
                    order.setRegister(command.getRegister());
                    order.setStatus("Active"); // Set the order status to "Active"
                    saveOrder(order); // Save the new order in the repository
                    message = "\nStarbucks Reserved Order" + "\n\n" +
                            "ID:    " + order.getId() + "\n" +
                            "Drink: " + order.getDrink() + "\n" +
                            "Milk:  " + order.getMilk() + "\n" +
                            "Size:  " + order.getSize() + "\n" +
                            "Total: " + order.getTotal() + "\n" +
                            "\n" +
                            "Register: " + order.getRegister() + "\n" +
                            "Status:   " + order.getStatus() + "\n\n";
                }
                break;
            // Get Order
            case "Get Order":
                Order activeOrder = null;
                for (Order existingOrder : orderModelRepository.findByRegister(String.valueOf(Long.valueOf(command.getRegister())))) {
                    if (existingOrder.getStatus().equals("Active")) {
                        activeOrder = existingOrder;
                        break;
                    }
                }

                if (activeOrder != null) {
                    message = "Starbucks Reserved Order" + "\n\n" +
                            "ID:    " + activeOrder.getId() + "\n" +
                            "Drink: " + activeOrder.getDrink() + "\n" +
                            "Milk:  " + activeOrder.getMilk() + "\n" +
                            "Size:  " + activeOrder.getSize() + "\n" +
                            "Total: " + activeOrder.getTotal() + "\n" +
                            "\n" +
                            "Register: " + activeOrder.getRegister() + "\n" +
                            "Status:   " + activeOrder.getStatus() + "\n\n";
                } else {
                    message = "Starbucks Reserved Order" + "\n\n" +
                            "Register: " + command.getRegister() + "\n" +
                            "Status:   " + "No Active Order" + "\n\n";
                }
                break;
            // Clear Order
            case "Clear Order":
                // Check if an order is active for this register
                boolean orderCleared = false;
                for (Order existingOrder : orderModelRepository.findByRegister(String.valueOf(Long.valueOf(command.getRegister())))) {
                    if (existingOrder.getStatus().equals("Active")) {
                        existingOrder.setStatus("Cleared"); // Set the order status to "Cleared"
                        saveOrder(existingOrder); // Update the order status in the repository
                        orderCleared = true;
                        message = "\nStarbucks Reserved Order" + "\n\n" +
                                "Register: " + command.getRegister() + "\n" +
                                "Status:   " + "Order Cleared" + "\n\n";
                        break;
                    }
                }
                if (!orderCleared) {
                    message = "No active order found for this register.";
                }
                break;
        }

        command.setMessage(message);
        System.out.println("\n----------------------------------------------------------------------------------------------------" +
                "\nAction: \n" + action);
        command.setRegister(command.getStores());
        System.out.println("\nCommand: \n" + command);



        String state = Order.GetNewOrder().getClass().getName();
        command.setState(state);
        saveOrder(Order.GetNewOrder());

        long ts_long = System.currentTimeMillis();
        String ts_string = String.valueOf(ts_long);

        command.setTimestamp(ts_string);
        String text = state + "/" + ts_string;
        String hash_string = hmac_sha256(key, text);

        command.setHash(hash_string);
        String server_ip;
        String host_name;

        try {
            InetAddress ip = InetAddress.getLocalHost();
            server_ip = ip.getHostAddress();
            host_name = ip.getHostName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("hash", hash_string);
        model.addAttribute("message", message);
        model.addAttribute("server", host_name + "/" + server_ip);

        return "starbucks";
    }
}