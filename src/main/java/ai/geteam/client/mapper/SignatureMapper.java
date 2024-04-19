package ai.geteam.client.mapper;

import ai.geteam.client.dto.SignatureDTO;
import ai.geteam.client.entity.signatue.Name;
import ai.geteam.client.entity.signatue.Signature;

public class SignatureMapper {
    private SignatureMapper(){}
    public static Signature toSignature(SignatureDTO signatureDTO){
        return Signature.builder()
                .id(signatureDTO.getId())
                .name(Enum.valueOf(Name.class,signatureDTO.getName()))
                .value(signatureDTO.getValue())
                .defaultValue(signatureDTO.isDefaultSign())
                .build();
    }

    public static SignatureDTO toSignatureDTO(Signature signature){
        return SignatureDTO.builder()
                .id(signature.getId())
                .name(signature.getName().name())
                .value(signature.getValue())
                .defaultSign(signature.isDefaultValue())
                .build();
    }
}
