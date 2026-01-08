package com.aaami.shared.command;

import com.aaami.cqrs.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductCommand implements Command<Void> {
    private Long id;
}

