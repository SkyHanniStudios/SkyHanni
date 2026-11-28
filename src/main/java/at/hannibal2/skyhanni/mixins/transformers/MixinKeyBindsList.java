package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.utils.SkyHanniKeyBindManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyBindsList.class)
public class MixinKeyBindsList {

    @ModifyVariable(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Ljava/util/Arrays;sort([Ljava/lang/Object;)V", shift = At.Shift.AFTER),
        name = "keyMappings"
    )
    private KeyMapping[] hideInactiveSkyHanniKeyBinds(KeyMapping[] keyMappings) {
        return SkyHanniKeyBindManager.INSTANCE.visibleOnly(keyMappings);
    }
}
