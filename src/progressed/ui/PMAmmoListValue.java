package progressed.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import progressed.*;
import progressed.content.*;
import progressed.entities.bullet.*;
import progressed.entities.bullet.InjectorBulletType.*;
import progressed.entities.units.*;

import static mindustry.Vars.*;

public class PMAmmoListValue<T extends UnlockableContent> implements StatValue{ //This is a copy of AmmoListValue that has modded stats attached to it.
    private final ObjectMap<T, BulletType> map;

    public PMAmmoListValue(ObjectMap<T, BulletType> map){
        this.map = map;
    }

    @Override
    public void display(Table table){

        table.row();

        for(T t : map.keys()){
            boolean unit = t instanceof UnitType;

            BulletType type = map.get(t);

            //no point in displaying unit icon twice
            if(!unit && !(t instanceof PowerTurret)){
                table.image(icon(t)).size(3 * 8).padRight(4).right().top();
                table.add(t.localizedName).padRight(10).left().top();
            }

            table.table(bt -> {
                bt.left().defaults().padRight(3).left();

                if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                    if(type instanceof BlackHoleBulletType stype){
                        bt.add(Core.bundle.format("bullet.pm-blackhole-damage", stype.continuousDamage(), Strings.fixed(stype.damageRadius / tilesize, 1)));
                        sep(bt, Core.bundle.format("bullet.pm-suction-radius", stype.suctionRadius / tilesize));
                    }else if(type.continuousDamage() > 0){
                        bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                    }else{
                        bt.add(Core.bundle.format("bullet.damage", type.damage));
                        if(type instanceof MagnetBulletType stype){
                            sep(bt, Core.bundle.format("bullet.pm-attraction-radius", stype.attractRange / tilesize));
                        }
                    }
                }

                if(type instanceof CritBulletType stype){
                    sep(bt, Core.bundle.format("bullet.pm-crit-chance", (int)(stype.critChance * 100f)));
                    sep(bt, Core.bundle.format("bullet.pm-crit-multiplier", (int)stype.critMultiplier));
                }

                if(type instanceof SignalFlareBulletType stype && stype.spawn instanceof FlareUnitType u){
                    sep(bt, Core.bundle.format("bullet.pm-flare-health", u.health));
                    sep(bt, Core.bundle.format("bullet.pm-flare-attraction", u.attraction));
                    sep(bt, Core.bundle.format("bullet.pm-flare-lifetime", (int)(u.duration / 60f)));
                }

                if(type.buildingDamageMultiplier != 1){
                    sep(bt, Core.bundle.format("bullet.buildingdamage", (int)(type.buildingDamageMultiplier * 100)));
                }

                if(type.splashDamage > 0){
                    sep(bt, Core.bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                }

                if(!unit && !Mathf.equal(type.ammoMultiplier, 1f) && !(type instanceof LiquidBulletType) && !(t instanceof PowerTurret)){
                    sep(bt, Core.bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
                }

                if(!Mathf.equal(type.reloadMultiplier, 1f)){
                    sep(bt, Core.bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)));
                }

                if(type.knockback > 0){
                    sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
                }

                if(type.healPercent > 0f){
                    sep(bt, Core.bundle.format("bullet.healpercent", (int)type.healPercent));
                }

                if(type.pierce || type.pierceCap != -1){
                    sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                }

                if(type.incendAmount > 0){
                    sep(bt, "@bullet.incendiary");
                }

                if(type.status != StatusEffects.none){
                    sep(bt, (type.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName);
                }

                if(type instanceof InjectorBulletType stype){ //This could probably be optimized, but stat display is only run once so whatever
                    Vaccine[] v = stype.vaccines;
                    StringBuilder str = new StringBuilder();
                    str.append("[lightgray]");

                    if(v.length == 1){ //Single
                        StatusEffect s = v[0].status;
                        str.append(s.minfo.mod == null ? s.emoji() : "")
                            .append("[stat]")
                            .append(s.localizedName);
                    }else if(v.length == 2){ //Double
                        StatusEffect s = v[0].status;
                        str.append(s.minfo.mod == null ? s.emoji() : "")
                            .append("[stat]")
                            .append(s.localizedName)
                            .append("[] or ");

                        s = v[1].status;
                        str.append(s.minfo.mod == null ? s.emoji() : "")
                            .append("[stat]")
                            .append(s.localizedName);
                    }else if(v.length > 2){ //3 or more
                        for(int i = 0; i < v.length - 1; i++){
                            StatusEffect s = v[i].status;
                            str.append(s.minfo.mod == null ? s.emoji() : "")
                                .append("[stat]")
                                .append(s.localizedName)
                                .append("[], ");
                        }

                        StatusEffect s = v[v.length - 1].status;
                        str.append("or ")
                            .append(s.minfo.mod == null ? s.emoji() : "")
                            .append("[stat]")
                            .append(s.localizedName);
                    }

                    sep(bt, str.toString());
                    if(stype.nanomachines){
                        bt.row();
                        bt.image(Core.atlas.find("prog-mats-nanomachines")).padTop(8f).scaling(Scaling.fit);
                    }
                }

                if(type.homingPower > 0.01f){
                    sep(bt, "@bullet.homing");
                }

                if(type instanceof CritBulletType stype && stype.bouncing){
                    sep(bt, "@bullet.pm-bouncing");
                }

                if(type.lightning > 0){
                    sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                }

                if(type instanceof UnitSpawnBulletType stype){
                    bt.row();
                    UnitType s = stype.spawn;
                    bt.image(s.icon(Cicon.full)).size(3 * 8);
                    bt.add("[stat]" + s.localizedName);
                }

                if(type instanceof UnitSpawnStrikeBulletType stype){
                    bt.row();
                    UnitType s = stype.spawn;
                    bt.image(s.icon(Cicon.full)).size(3 * 8);   
                    bt.add("[stat]" + s.localizedName);
                }

                if(type.fragBullet != null){
                    sep(bt, "@bullet.frag");
                }
            }).padTop(unit ? 0 : -9).left().get().background(unit ? null : Tex.underline);

            table.row();
        }
    }

    void sep(Table table, String text){
        table.row();
        table.add(text);
    }

    TextureRegion icon(T t){
        return t.icon(Cicon.medium);
    }
}