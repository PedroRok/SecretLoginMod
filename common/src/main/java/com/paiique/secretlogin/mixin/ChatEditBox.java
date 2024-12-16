package com.paiique.secretlogin.mixin;

import com.paiique.secretlogin.Secretlogin;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.BiFunction;

// Todo: Needs refactoring
@Mixin(EditBox.class)
public abstract class ChatEditBox extends AbstractWidget {

    public ChatEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    public abstract boolean isBordered();

    @Final
    @Shadow
    private static WidgetSprites SPRITES;

    @Shadow
    private int textColor;

    @Shadow
    private int textColorUneditable;

    @Shadow
    private int cursorPos;

    @Shadow
    private int displayPos;

    @Shadow
    private String value;

    @Shadow
    private boolean isEditable;

    @Final
    @Shadow
    private Font font;

    @Shadow
    public abstract int getInnerWidth();

    @Shadow
    private long focusedTime;

    @Shadow
    private boolean bordered;

    @Shadow
    private int highlightPos;

    @Shadow
    private Component hint;

    @Shadow
    private String suggestion;

    @Shadow
    protected abstract void renderHighlight(GuiGraphics guiGraphics, int i, int j, int k, int l);

    @Shadow
    private BiFunction<String, Integer, FormattedCharSequence> formatter;

    @Shadow
    protected abstract int getMaxLength();

    @Inject(method = "renderWidget", at = @At(value = "HEAD"), cancellable = true)
    private void renderWidget(GuiGraphics guiGraphics, int xPos, int yPos, float deltaTime, CallbackInfo ci) {
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) return;
        ci.cancel();
        if (this.isVisible()) {
            if (this.isBordered()) {
                ResourceLocation spriteLocation = SPRITES.get(this.isActive(), this.isFocused());
                guiGraphics.blitSprite(RenderType::guiTextured, spriteLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }

            int textColor = this.isEditable ? this.textColor : this.textColorUneditable;
            int cursorOffset = this.cursorPos - this.displayPos;
            String visibleText = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean isCursorInRange = cursorOffset >= 0 && cursorOffset <= visibleText.length();
            boolean isCursorBlinking = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && isCursorInRange;
            int leftPos = this.bordered ? this.getX() + 4 : this.getX();
            int topPos = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            int currentXPos = leftPos;
            int highlightEnd = Mth.clamp(this.highlightPos - this.displayPos, 0, visibleText.length());

            if (!visibleText.isEmpty()) {
                String textBeforeCursor = isCursorInRange ? visibleText.substring(0, cursorOffset) : visibleText;
                currentXPos = guiGraphics.drawString(this.font, this.formatter.apply(secretlogin$protectPassword(textBeforeCursor), this.displayPos), currentXPos, topPos, textColor);
            }

            boolean hasTextOverflow = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int highlightStart = currentXPos;
            if (!isCursorInRange) {
                highlightStart = cursorOffset > 0 ? leftPos + this.width : leftPos;
            } else if (hasTextOverflow) {
                --highlightStart;
                --currentXPos;
            }

            if (!visibleText.isEmpty() && isCursorInRange && cursorOffset < visibleText.length()) {
                guiGraphics.drawString(this.font, this.formatter.apply(visibleText.substring(cursorOffset), this.cursorPos), currentXPos, topPos, textColor);
            }

            if (this.hint != null && visibleText.isEmpty() && !this.isFocused()) {
                guiGraphics.drawString(this.font, this.hint, currentXPos, topPos, textColor);
            }

            if (!hasTextOverflow && this.suggestion != null) {
                guiGraphics.drawString(this.font, this.suggestion, highlightStart - 1, topPos, -8355712);
            }

            if (isCursorBlinking) {
                if (hasTextOverflow) {
                    RenderType overlayType = RenderType.guiOverlay();
                    int upperPos = topPos - 1;
                    int lowerPos = highlightStart + 1;
                    int bottomPos = topPos + 1;
                    Objects.requireNonNull(this.font);
                    guiGraphics.fill(overlayType, highlightStart, upperPos, lowerPos, bottomPos + 9, -3092272);
                } else {
                    guiGraphics.drawString(this.font, "_", highlightStart, topPos, textColor);
                }
            }

            if (highlightEnd != cursorOffset) {
                int highlightPosX = leftPos + this.font.width(visibleText.substring(0, highlightEnd));
                int highlightTop = topPos - 1;
                int highlightLeft = highlightPosX - 1;
                int highlightBottom = topPos + 1;
                Objects.requireNonNull(this.font);
                this.renderHighlight(guiGraphics, highlightStart, highlightTop, highlightLeft, highlightBottom + 9);
            }
        }
    }

    @Unique
    private String secretlogin$protectPassword(String textBeforeCursor) {
        boolean found = false;
        for (String command : Secretlogin.getConfig().getCommandFilter()) {
            if (textBeforeCursor.startsWith(command)) {
                found = true;
                break;
            }
        }
        if (!found) return textBeforeCursor;

        String command = textBeforeCursor.replaceAll("(?<=\\s)(\\S+)", "");
        String password = textBeforeCursor.replaceAll(".*\\s", "");
        int asterisksCount = password.length();

        password = "*".repeat(asterisksCount);
        return command + password;
    }
}
