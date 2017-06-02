package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Vitalii Parfonov
 */
@DTO
public interface FoundItem {

    ItemReference getItemReference();

    void setItemReference(ItemReference itemReference);

    FoundItem withItemReference(ItemReference itemReference);

    List<FoundItemData> getFoundItemData();

    void setFoundItemData(List<FoundItemData> foundItemData);

    FoundItem withFoundItemData(List<FoundItemData> foundItemData);





}
