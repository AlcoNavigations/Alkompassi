package fi.metropolia.alkompassi.Remote

object Model {
    data class Result(val candidates: List<Candidates>)
    data class Candidates(val formatted_address: String)

}