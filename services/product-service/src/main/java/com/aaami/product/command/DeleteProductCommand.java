package com.aaami.product.command;

import com.aaami.cqrs.Command;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductCommand implements Command<Void> {
    @NotNull(message = "Product ID is required")
    private Long id;
}

