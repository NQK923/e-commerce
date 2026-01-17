package com.learnfirebase.ecommerce.product.application.port.in;

import java.io.InputStream;
import com.learnfirebase.ecommerce.product.application.dto.BulkUploadResult;

public interface BulkProductUploadUseCase {
    BulkUploadResult uploadBulk(String sellerId, InputStream csvStream);
}
