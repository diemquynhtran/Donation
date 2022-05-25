package com.example.models;

public class Donation
{
    public String _id;
    public int amount;
    public String method;
    public int upvotes;
    public Donation (int amount, String method, int upvotes)
    {
        this.amount = amount;
        this.method = method;
        this.upvotes = upvotes;
    }
    public Donation ()
    {
        this.amount = 0;
        this.method = "";
        this.upvotes = 0;
    }
    public String toString()
    {
        return _id + ", " + amount + ", " + method + ", " + upvotes;
    }
}