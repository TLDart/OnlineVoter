package OnlineVoter;
class VotingListInfo{
    private String name;
    private long voteCount;

    VotingListInfo(String name){
        this.name = name;
        this.voteCount = 0;
    }

    void addVoteCount(){
        this.voteCount++;
    }

    long getVoteCount(){
        return this.voteCount;
    }

    String getName(){
        return this.name;
    }
}