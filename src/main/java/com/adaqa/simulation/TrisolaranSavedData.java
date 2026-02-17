package com.adaqa.simulation;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;

import java.util.List;

public class TrisolaranSavedData extends PersistentState {
    private static final String ID = "trisolaran_simulation";
    private final ThreeBodySystem system = TrisolaranSystem.getSystem();

    public static TrisolaranSavedData getServerState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(
                        TrisolaranSavedData::new,
                        TrisolaranSavedData::fromNbt,
                        null // No DataFixTypes needed for now
                ),
                ID
        );
    }
    
    public TrisolaranSavedData() {
        // Default constructor
    }

    public static TrisolaranSavedData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        TrisolaranSavedData data = new TrisolaranSavedData();
        ThreeBodySystem system = data.system;
        
        if (nbt.contains("bodies", NbtElement.LIST_TYPE)) {
            NbtList bodiesList = nbt.getList("bodies", NbtElement.COMPOUND_TYPE);
            List<ThreeBodySystem.Body> bodies = system.getBodies();
            
            // Ensure size matches or recreate
            if (bodiesList.size() != bodies.size()) {
                 bodies.clear();
                 for(int i=0; i<bodiesList.size(); i++) {
                     bodies.add(new ThreeBodySystem.Body(Vec3d.ZERO, Vec3d.ZERO, 1.0));
                 }
            }
            
            for (int i = 0; i < bodiesList.size(); i++) {
                NbtCompound bodyNbt = bodiesList.getCompound(i);
                ThreeBodySystem.Body body = bodies.get(i);
                
                body.position = new Vec3d(
                    bodyNbt.getDouble("posX"),
                    bodyNbt.getDouble("posY"),
                    bodyNbt.getDouble("posZ")
                );
                
                body.velocity = new Vec3d(
                    bodyNbt.getDouble("velX"),
                    bodyNbt.getDouble("velY"),
                    bodyNbt.getDouble("velZ")
                );
                
                body.mass = bodyNbt.getDouble("mass");
            }
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList bodiesList = new NbtList();
        for (ThreeBodySystem.Body body : system.getBodies()) {
            NbtCompound bodyNbt = new NbtCompound();
            bodyNbt.putDouble("posX", body.position.x);
            bodyNbt.putDouble("posY", body.position.y);
            bodyNbt.putDouble("posZ", body.position.z);
            
            bodyNbt.putDouble("velX", body.velocity.x);
            bodyNbt.putDouble("velY", body.velocity.y);
            bodyNbt.putDouble("velZ", body.velocity.z);
            
            bodyNbt.putDouble("mass", body.mass);
            bodiesList.add(bodyNbt);
        }
        nbt.put("bodies", bodiesList);
        return nbt;
    }
    
    public void markDirty() {
        super.markDirty();
    }
}
