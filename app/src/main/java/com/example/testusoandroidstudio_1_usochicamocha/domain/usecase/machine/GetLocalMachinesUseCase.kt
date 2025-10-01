package com.example.testusoandroidstudio_1_usochicamocha.domain.usecase.machine

import com.example.testusoandroidstudio_1_usochicamocha.domain.model.Machine
import com.example.testusoandroidstudio_1_usochicamocha.domain.repository.MachineRepository
import com.example.testusoandroidstudio_1_usochicamocha.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetLocalMachinesUseCase @Inject constructor(
    private val machineRepository: MachineRepository
) {
    operator fun invoke(): Flow<Resource<List<Machine>>> = flow {
        try {
            emit(Resource.Loading())
            // Assuming getLocalMachines() in the repository returns a Flow<List<Machine>>
            // If it returns List<Machine> directly, adjust accordingly.
            machineRepository.getLocalMachines().collect { machines ->
                emit(Resource.Success(machines))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}
